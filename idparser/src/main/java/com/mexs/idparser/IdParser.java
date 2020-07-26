package com.mexs.idparser;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdParser {

    // Regex patterns to extract ID fields
    private Pattern patternName = Pattern.compile("(?im)(?:NAME)\\n([A-Z ,\\-']*)$");
    private Pattern patternSgId = Pattern.compile("(?i)\\b([STFG][\\d]{7}[A-Z])\\b");
    private Pattern patternNationality = Pattern.compile("(?im)(?:NATIONALITY|COUNTRY[\\w ]*)\\n([A-Z ]*)$");
    private Pattern patternDob = Pattern.compile("(?i)\\b((0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012]|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)[- /.](19|20)\\d\\d)\\b");
    private Pattern patternGender = Pattern.compile("(?im)(?:SEX|GENDER)\\s([FM]{1}|FEMALE|MALE)$");
    private Pattern patternAddress = Pattern.compile("(?i)\\b((?:APT|BLK|BLOCK|\\d{1,4}[A-Z]? )[\\s\\S]+(?:SINGAPORE \\d{6}))\\b");
    private Pattern patternPassportMRZ = Pattern.compile("\\b(P[A-Z0-9<]{43}\\n[A-Z0-9<]{44})\\b");

    // For nationality fields
    public enum NAT_RETURN_TYPE{NATIONALITY, COUNTRY_NAME, COUNTRY_CODE}
    private NAT_RETURN_TYPE mNatReturnType;
    private String[][] mTopNat;                 // 0: Nationality, 1: Country, 2: Country Code

    // Confidence checks
    private ConfidenceCheckString mConfidenceCheckName;
    private ConfidenceCheckSgId mConfidenceCheckSgId;
    private ConfidenceCheckNat mConfidenceCheckNat;
    private ConfidenceCheckDob mConfidenceCheckDob;
    private ConfidenceCheckString mConfidenceCheckAddress;

    private IdData mIdData;

    public IdParser(int curYear, String[][] mTopNat, NAT_RETURN_TYPE mNatReturnType) {
        this.mTopNat = mTopNat;
        this.mNatReturnType = mNatReturnType;

        this.mConfidenceCheckName = new ConfidenceCheckString(6, 3);
        this.mConfidenceCheckSgId = new ConfidenceCheckSgId();
        this.mConfidenceCheckNat = new ConfidenceCheckNat();
        this.mConfidenceCheckDob = new ConfidenceCheckDob(curYear);
        this.mConfidenceCheckAddress = new ConfidenceCheckString(6, 3);

        this.mIdData = new IdData();
    }

    public IdData process(String text){
        // Extract Passport MRZ, if available
        Matcher matchPassportMRZ = patternPassportMRZ.matcher(text.replaceAll(" ", ""));
        if (matchPassportMRZ.find()){
            String passportMRZ = matchPassportMRZ.group(1);

            // Extract Name
            mIdData.setName(passportMRZ.substring(5, 45).replaceAll("[\\s<«]+", " ").trim());
            mIdData.setNameConfident(true);

            // Extract Passport No.
            String detectedPassportNo = passportMRZ.substring(45, 54);
            char detectedPassportChecksum = passportMRZ.charAt(54);

            if (checksumPassport(detectedPassportNo, detectedPassportChecksum)){
                mIdData.setSgId(detectedPassportNo);
                mIdData.setSgIdConfident(true);
            }

            // Extract DOB
            String detectedDob = passportMRZ.substring(58, 64);
            char detectedDobChecksum = passportMRZ.charAt(64);

            if (checksumPassport(detectedDob, detectedDobChecksum)){
                mIdData.setDob(detectedDob);
                mIdData.setDobConfident(true);
            }

            // Extract Gender
            String detectedGender = passportMRZ.substring(65, 66);

            if (detectedGender.matches("[MF]")){
                mIdData.setGender(detectedGender);
                mIdData.setGenderConfident(true);
            }

            // Extract Nationality
            String detectedNationality = passportMRZ.substring(55, 58);
            String matchTopNat = mConfidenceCheckNat.getConfidentNat(detectedNationality);

            mIdData.setNationality(matchTopNat.isEmpty() ? detectedNationality : matchTopNat);
            mIdData.setNationalityConfident(!matchTopNat.isEmpty());
        }

        // Extract Name
        if (!mIdData.isNameConfident()){
            Matcher matchName = patternName.matcher(text);
            if (matchName.find()){
                // Add detected string into running list
                mConfidenceCheckName.add(matchName.group(1));

                // Show name with the current highest count
                mIdData.setName(mConfidenceCheckName.getHighest());
                mIdData.setNameConfident(!mConfidenceCheckName.getConfident().isEmpty());
            }
        }

        // Extract SG ID No.
        if (!mIdData.isSgIdConfident()) {
            Matcher matchSgId = patternSgId.matcher(text);
            if (matchSgId.find()){
                String confidentSgId = mConfidenceCheckSgId.getConfidentSgId(matchSgId.group(1));

                mIdData.setSgId(confidentSgId.isEmpty() ? matchSgId.group(1) : confidentSgId);
                mIdData.setSgIdConfident(!confidentSgId.isEmpty());
            }
        }

        // Extract Nationality
        if (!mIdData.isNationalityConfident()){
            Matcher matchNationality = patternNationality.matcher(text);
            if (matchNationality.find()){
                String matchTopNat = mConfidenceCheckNat.getConfidentNat(matchNationality.group(1));

                mIdData.setNationality(matchTopNat.isEmpty() ? matchNationality.group(1) : matchTopNat);
                mIdData.setNationalityConfident(!matchTopNat.isEmpty());
            }
        }

        // Extract DOB
        if (!mIdData.isDobConfident()){
            Matcher matchDob = patternDob.matcher(text);
            if (matchDob.find()){
                String confidentDob = mConfidenceCheckDob.getConfidentDob(matchDob.group(1), mIdData.getSgId());

                mIdData.setDob(confidentDob.isEmpty() ? matchDob.group(1) : confidentDob);
                mIdData.setDobConfident(!confidentDob.isEmpty());
            }
        }

        // Extract Gender
        if (!mIdData.isGenderConfident()){
            Matcher matchGender = patternGender.matcher(text);
            if (matchGender.find()){
                mIdData.setGender(matchGender.group(1));
                mIdData.setGenderConfident(true);
            }
        }

        // Extract Address
        if (!mIdData.isAddressConfident()){
            Matcher matchAddress = patternAddress.matcher(text);
            if (matchAddress.find()){
                // Add detected string into running list
                mConfidenceCheckAddress.add(matchAddress.group(1));

                // Show address with the current highest count
                mIdData.setAddress(mConfidenceCheckAddress.getHighest());
                mIdData.setAddressConfident(!mConfidenceCheckAddress.getConfident().isEmpty());
            }
        }

        return mIdData;
    }

    public void clear(){
        mIdData = new IdData();
    }

    private class ConfidenceCheckString{
        private HashMap<String, Integer> stringCount;
        private int maxQueueSize;
        private int minCount;

        ConfidenceCheckString(int maxQueueSize, int minCount) {
            this.stringCount = new HashMap<>();
            this.maxQueueSize = maxQueueSize;
            this.minCount = minCount;
        }

        String getConfident(){
            if (stringCount.size() == 0)
                return "";

            String key = getHighest();
            int value = stringCount.get(key);

            return value >= minCount ? key : "";
        }

        String getHighest(){
            String textMax = "";
            Integer curMax = 0;
            for (Map.Entry<String, Integer> entry: stringCount.entrySet()){
                if (entry.getValue() > curMax) {
                    textMax = entry.getKey();
                    curMax = entry.getValue();
                }
            }

            return textMax;
        }

        String getLowest(){
            String textMin = "";
            Integer curMin = 100;
            for (Map.Entry<String, Integer> entry: stringCount.entrySet()){
                if (entry.getValue() < curMin) {
                    textMin = entry.getKey();
                    curMin = entry.getValue();
                }
            }

            return textMin;
        }

        private void upsert(String text){
            if (stringCount.containsKey(text)){
                stringCount.put(text, stringCount.get(text)+1);
            } else {
                stringCount.put(text, 1);
            }
        }

        void add(String text){
            if (text == null || text.isEmpty() || text.length() <= 3)
                return;

            // Add to HashMap
            if (stringCount.size() <= maxQueueSize){
                upsert(text);
            } else {
                // Remove string with lowest count
                stringCount.remove(getLowest());
                upsert(text);
            }
        }
    }

    private class ConfidenceCheckSgId{
        private final char[] seriesNRIC = {'J',  'Z',  'I',  'H',  'G',  'F',  'E',  'D',  'C',  'B',  'A'};
        private final char[] seriesFIN = {'X',  'W',  'U',  'T',  'R',  'Q',  'P',  'N',  'M',  'L',  'K'};

        String getConfidentSgId(String detectedText){
            if (detectedText == null || detectedText.length() != 9)
                return "";

            char[] detectedChars = detectedText.toCharArray();
            int sum = 0;

            sum += (detectedChars[0] == 'T' || detectedChars[0] == 'G') ? 4 : 0;
            sum += Character.getNumericValue(detectedChars[1]) * 2;
            sum += Character.getNumericValue(detectedChars[2]) * 7;
            sum += Character.getNumericValue(detectedChars[3]) * 6;
            sum += Character.getNumericValue(detectedChars[4]) * 5;
            sum += Character.getNumericValue(detectedChars[5]) * 4;
            sum += Character.getNumericValue(detectedChars[6]) * 3;
            sum += Character.getNumericValue(detectedChars[7]) * 2;

            int checkval = sum % 11;

            char checksum = '0';
            if (detectedChars[0] == 'S' || detectedChars[0] == 'T'){
                checksum = seriesNRIC[checkval];
            } else if (detectedChars[0] == 'F' || detectedChars[0] == 'G'){
                checksum = seriesFIN[checkval];
            }

            return (checksum != '0' && detectedChars[8] == checksum) ? detectedText : "";
        }
    }

    private class ConfidenceCheckNat{
        private JaroWinklerSimilarity mStringComparator;

        ConfidenceCheckNat() {
            mStringComparator = new JaroWinklerSimilarity();
        }

        String getConfidentNat(String detectedText){
            if (detectedText == null || detectedText.isEmpty())
                return "";

            detectedText = detectedText.toUpperCase();

            // Match against ISO 3166-1 alpha-3 country code
            if (detectedText.length() == 3){
                for (int i=0; i<mTopNat.length; i++){
                    if (detectedText.equals(mTopNat[i][2]))
                        return getNatReturn(i, mNatReturnType);
                }
            }

            // Match against nationality
            for (int i=0; i<mTopNat.length; i++){
                if (mStringComparator.apply(detectedText, mTopNat[i][0]) > 0.86){
                    return getNatReturn(i, mNatReturnType);
                }
            }

            // Match against country name
            for (int i=0; i<mTopNat.length; i++){
                if (mStringComparator.apply(detectedText, mTopNat[i][1]) > 0.86){
                    return getNatReturn(i, mNatReturnType);
                }
            }

            // No match found
            return "";
        }

        private String getNatReturn(int i, NAT_RETURN_TYPE returnType){
            switch (returnType){
                case COUNTRY_NAME:
                    return mTopNat[i][1];
                case COUNTRY_CODE:
                    return mTopNat[i][2];
                case NATIONALITY:
                default:
                    return mTopNat[i][0];
            }
        }
    }

    private class ConfidenceCheckDob{
        private int curYear;

        ConfidenceCheckDob(int curYear) {
            this.curYear = curYear;
        }

        String getConfidentDob(String detectedText, String detectedSgId){
            if (detectedText == null || detectedText.isEmpty())
                return "";

            detectedSgId = detectedSgId == null ? "" : detectedSgId.toUpperCase();
            int dobYear = Integer.parseInt(detectedText.substring(6, 10));
            int sgIdYear = 0;

            if (!detectedSgId.isEmpty()){
                switch (detectedSgId.charAt(0)){
                    case 'S':
                        sgIdYear = Integer.parseInt("19" + detectedSgId.substring(1,3));
                        break;
                    case 'T':
                        sgIdYear = Integer.parseInt("20" + detectedSgId.substring(1,3));
                        break;
                }
            }

            if (detectedSgId.length() != 9)
                return "";

            switch (detectedSgId.charAt(0)){
                case 'F':
                case 'G':
                    if (dobYear <= curYear - 18)
                        return detectedText;

                    break;
                case 'S':
                case 'T':
                    if (dobYear < 1968) {
                        return detectedText;
                    } else if (dobYear == sgIdYear){
                        return detectedText;
                    }

                    break;
            }

            return "";
        }
    }

    private static Boolean checksumPassport(String text, char checksum){
        int[] weights = {7, 3, 1};
        int sum = 0;

        for (int i=0; i<text.length(); i++){
            char detectedTextChar = text.charAt(i);
            sum += (Character.isDigit(detectedTextChar) ? Character.getNumericValue(detectedTextChar) : (int) detectedTextChar - 55) * weights[i%3];
        }

        int checkval = sum % 10;

        return checkval == Character.getNumericValue(checksum);
    }

}