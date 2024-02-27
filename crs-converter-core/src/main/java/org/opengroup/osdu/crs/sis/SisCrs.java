package org.opengroup.osdu.crs.sis;

import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.IdentifiedObjects;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;
import org.opengroup.osdu.crs.util.Constants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SisCrs implements ISisCrs {

    private final CoordinateReferenceSystem coordianteReferenceSystem;
    private final boolean negativeScale;
    private final String wkt;
    private final AuthorityCode authorityCode;
    private final String name;
    private final boolean isProjection;
    private ISisCrs baseCrs;

    public SisCrs(CoordinateReferenceSystem coordianteReferenceSystem, boolean negativeScale, String wkt, AuthorityCode code, String name) {
        this.coordianteReferenceSystem = coordianteReferenceSystem;
        this.negativeScale = negativeScale;
        this.wkt = wkt;
        this.authorityCode = code;
        this.name = name;
        this.isProjection = coordianteReferenceSystem instanceof ProjectedCRS;
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return coordianteReferenceSystem;
    }

    @Override
    public boolean isNegativeScale() {
        return negativeScale;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ISisCrs getGeogCoordSys() throws IllegalArgumentException {
        if (!isProjection) {
            return null;
        }
        if (baseCrs == null) {
            try {
                GeographicCRS baseCRS = ((ProjectedCRS) coordianteReferenceSystem).getBaseCRS();
                String baseAuthority = "";
                String baseCodeString = "";
                String baseWKT = baseCRS.toWKT();
                Identifier identifier = IdentifiedObjects.getIdentifier(baseCRS, Citations.EPSG);

                if (identifier != null && identifier.getCode() != null) {
                    baseCodeString = identifier.getCode();
                    baseAuthority = "EPSG";
                }
                AuthorityCode baseAuthorityCode = new AuthorityCode(baseAuthority, baseCodeString);
                String baseName = CrsNameUtils.findBaseCrsName(wkt, baseWKT, "");
                SisCrsFactory sisFactory = new SisCrsFactory();
                baseCrs = sisFactory.createSisCrs(baseWKT, baseAuthorityCode, baseName);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Can't find base crs from projected crs", ex);
            }
        }

        //get the base crs
        return baseCrs;
    }

    @Override
    public boolean isEqual(ISisCrs otherSISCrs) {
        Identifier identifier = IdentifiedObjects.getIdentifier(coordianteReferenceSystem, Citations.EPSG);
        Identifier otherIdentifier = IdentifiedObjects.getIdentifier(otherSISCrs.getCoordinateReferenceSystem(), Citations.EPSG);
        if (identifier != null && otherIdentifier != null) {
            return identifier.getCode().equals(otherIdentifier.getCode());
        }

        return coordianteReferenceSystem.equals(otherSISCrs.getCoordinateReferenceSystem());
    }

    @Override
    public boolean isEqual(CoordinateReferenceSystem transformSourceCRS, CoordinateReferenceSystem transformTargetCRS, ISisCrs fromBaseCrs, ISisCrs toBaseCrs) {
        Identifier identifier = IdentifiedObjects.getIdentifier(coordianteReferenceSystem, Citations.EPSG);
        Identifier otherIdentifier = IdentifiedObjects.getIdentifier(transformSourceCRS, Citations.EPSG);
        if (identifier != null && otherIdentifier != null) {
            return identifier.getCode().equals(otherIdentifier.getCode());
        }
        boolean do_reverse = false;
        if (identifier == null || otherIdentifier == null) {
            String fromCRS = fromBaseCrs.getName();
            String toCRS = toBaseCrs.getName();
            String sourceCRS = String.valueOf(transformSourceCRS.getName().getCode());
            String targetCRS = String.valueOf(transformTargetCRS.getName().getCode());
            //we are defining a new algorithm if the above conditions is failing. In this method we are comapring the
            // characters and getting score. By using the score we are deciding to reverse or forword the transformation
            //Compare and get the score for fromCRS, sourceCRS
            int score_CTsource_is_fromCRS = compareCharacters(fromCRS, sourceCRS);
            //Compare and get the score for toCRS, sourceCRS
            int score_CTsource_is_toCRS = compareCharacters(toCRS, sourceCRS);
            //Compare and get the score for fromCRS, targetCRS
            int score_CTtarget_is_fromCRS = compareCharacters(fromCRS, targetCRS);
            //Compare and get the score for toCRS, targetCRS
            int score_CTtarget_is_toCRS = compareCharacters(toCRS, targetCRS);
            LinkedHashMap<String, Integer> temp_map = new LinkedHashMap<String, Integer>();
            temp_map.put("score_CTsource_is_fromCRS", score_CTsource_is_fromCRS);
            temp_map.put("score_CTsource_is_toCRS", score_CTsource_is_toCRS);
            temp_map.put("score_CTtarget_is_fromCRS", score_CTtarget_is_fromCRS);
            temp_map.put("score_CTtarget_is_toCRS", score_CTtarget_is_toCRS);

            do_reverse = findDirection(temp_map);
        }

       return do_reverse;
    }

    public int compareCharacters(String crsname, String othercrsname){
        //Step1- Remove all the spaces and underscores(special characters)(- and /)GCS
        String formattedinput =removingSpacesFromName(crsname);
        String Otherformattedinput =removingSpacesFromName(othercrsname);
        //Step2- score equal the number of characters match from left to right
        int left_right_score= characterMatchLefttoRight(formattedinput,Otherformattedinput);
        //Score + equals the number of characters match from right to left
        int right_left_score= characterMatchRighttoLeft(formattedinput,Otherformattedinput);
        //return the total score
        //sum the charactersmatching from left to right
        int score = left_right_score +right_left_score;
        return score;
    }

    private boolean findDirection(LinkedHashMap<String, Integer> temp_map) {
        boolean direction_flag = false;
        ArrayList<Integer> tempList = (ArrayList<Integer>) temp_map.values().stream().collect(Collectors.toList());
        int max_score = tempList.stream().mapToInt(Integer::intValue).max().getAsInt();
        //if max_score is 0 then throw an exception.
        if (max_score == 0) {
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_TRANSFORM_CRS_MATCH);
        }
        // based on the score it will check the below if-else conditions
        if (temp_map.get("score_CTtarget_is_toCRS") > temp_map.get("score_CTtarget_is_fromCRS")
                && temp_map.get("score_CTsource_is_fromCRS") > temp_map.get("score_CTsource_is_toCRS")) {
            direction_flag = false;// forward direction
        } else if (temp_map.get("score_CTtarget_is_fromCRS") > temp_map.get("score_CTtarget_is_toCRS")
                && temp_map.get("score_CTsource_is_toCRS") > temp_map.get("score_CTsource_is_fromCRS")) {
            direction_flag = true;// reverse direction
        } else {// It will work to throw an exception
            throw new IllegalArgumentException(Constants.ERROR_MSG_INVALID_TRANSFORM_CRS_MATCH);
        }
        return direction_flag;
    }


    public boolean isEqual(CoordinateReferenceSystem otherCoordinateReferenceSystem) {
        // for epsg codes.
        Identifier identifier = IdentifiedObjects.getIdentifier(coordianteReferenceSystem, Citations.EPSG);
        Identifier otherIdentifier = IdentifiedObjects.getIdentifier(otherCoordinateReferenceSystem, Citations.EPSG);
        if (identifier != null && otherIdentifier != null) {
            return identifier.getCode().equals(otherIdentifier.getCode());
        }

        // non -epsg codes
        if (identifier == null || otherIdentifier == null) {
            String Crs_Name = String.valueOf(coordianteReferenceSystem.getName().getCode());
            String other_Crs_Name = String.valueOf(otherCoordinateReferenceSystem.getName().getCode());
            //we are defining a new algorithm if the above conditions is failing. In this method we are comapring the
            // characters and getting score. By using the score we are deciding to reverse or forword the transformation
           //return CompareCharacters(Crs_Name,other_Crs_Name);


        }
        return coordianteReferenceSystem.equals(otherCoordinateReferenceSystem);
    }

    public int CompareCharacters(String crsname, String othercrsname){
        //Step1- Remove all the spaces and underscores(special characters)(- and /)GCS
        String formattedinput =RemovingSpacesFromName(crsname);
        String Otherformattedinput =RemovingSpacesFromName(othercrsname);
        //Step2- score equal the number of characters match from left to right
        /*int left_right_score= CharacterMatchLefttoRight(formattedinput,Otherformattedinput);
        //Score + equals the number of characters match from right to left
        int right_left_score= CharacterMatchRighttoLeft(formattedinput,Otherformattedinput);
        //return the total score
        //sum the charactersmatching from left to right
        int score = left_right_score +right_left_score;*/

        return 0;
    }

    public int characterMatchLefttoRight(String formattedinput, String otherformattedinput) {
        int otherformattedinputLength = otherformattedinput.length();
        int formattedinputLength = formattedinput.length();
        int count = 0;
        // start iterating from the beginning of both
        int i = 0;
        int j = 0;
        while (i < formattedinputLength && j < otherformattedinputLength
                && formattedinput.charAt(i) == otherformattedinput.charAt(j)) {
            count++;
            i++;
            j++;
        }
        return count;
    }
    private int characterMatchRighttoLeft(String formattedinput, String otherformattedinput) {
        int count = 0;
        // start iterating from the beginning of both
        int i = formattedinput.length() - 1;
        int j = otherformattedinput.length() - 1;
        while (i >= 0 && j >= 0 && formattedinput.charAt(i) == otherformattedinput.charAt(j)) {
            count++;
            i--;
            j--;
        }
        return count;
    }

    public String RemovingSpacesFromName(String input) {
        String regex = "GCS_|[-_ /\\\\[\\\\]()]";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(input);
        String updatedName = match.replaceAll("");
        updatedName = updatedName.replaceAll("\\s+", "");
        return updatedName.toUpperCase();
    }

    public String removingSpacesFromName(String input) {
        String regex = "GCS_|[-_ /\\\\[\\\\]()]";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(input);
        String updatedName = match.replaceAll("");
        updatedName = updatedName.replaceAll("\\s+", "");
        return updatedName.toUpperCase();
    }



    @Override
    public String toString() {
        return getWkt();
    }

    @Override
    public String getWkt() {
        if (wkt != null) {
            return wkt;
        }
        return coordianteReferenceSystem.toWKT();
    }

    @Override
    public AuthorityCode getAuthorityCode() {
        return authorityCode;
    }
}
