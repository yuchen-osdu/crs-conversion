package org.opengroup.osdu.crs.sis;

import org.apache.sis.metadata.iso.citation.Citations;
import org.apache.sis.referencing.IdentifiedObjects;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengroup.osdu.crs.model.Impl.AuthorityCode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public boolean isEqual(CoordinateReferenceSystem otherCoordinateReferenceSystem) {
        Identifier identifier = IdentifiedObjects.getIdentifier(coordianteReferenceSystem, Citations.EPSG);
        Identifier otherIdentifier = IdentifiedObjects.getIdentifier(otherCoordinateReferenceSystem, Citations.EPSG);
        if (identifier != null && otherIdentifier != null) {
            return identifier.getCode().equals(otherIdentifier.getCode());
        }
        if (identifier == null || otherIdentifier == null) {
            String Crs_Name = String.valueOf(coordianteReferenceSystem.getName().getCode());
            String other_Crs_Name = String.valueOf(otherCoordinateReferenceSystem.getName().getCode());
            //we are defining a new algorithm if the above conditions is failing. In this method we are comapring the
            // characters and getting score. By using the score we are deciding to reverse or forword the transformation
           return CompareCharacters(Crs_Name,other_Crs_Name);


        }
        return coordianteReferenceSystem.equals(otherCoordinateReferenceSystem);
    }

    public boolean CompareCharacters(String crsname, String othercrsname){
        //Step1- Remove all the spaces and underscores(special characters)(- and /)GCS
        String formattedinput =RemovingSpacesFromName(crsname);
        String Otherformattedinput =RemovingSpacesFromName(othercrsname);
        //Step2- score equal the number of characters match from left to right
        int left_right_score= CharacterMatchLefttoRight(formattedinput,Otherformattedinput);
        //Score + equals the number of characters match from right to left
        int right_left_score= CharacterMatchRighttoLeft(formattedinput,Otherformattedinput);
        //return the total score
        //sum the charactersmatching from left to right
        int score = left_right_score +right_left_score;
        if(score >0){
            return true;
        }
        return false;
    }

    public int CharacterMatchLefttoRight(String formattedinput, String otherformattedinput) {
        int count =0;
        for(int i=0; i<formattedinput.length();i++){

            if(formattedinput.charAt(i)==otherformattedinput.charAt(i)){
                count++;
            }else{
                break;
            }
        }
        return count;
    }
    private int CharacterMatchRighttoLeft(String formattedinput, String otherformattedinput) {
        int length = otherformattedinput.length() - 1;
        int count = 0;
        for (int i = formattedinput.length() - 1; i >= 0; i--) {

            if (formattedinput.charAt(i) == otherformattedinput.charAt(length)) {
                count++;
                length--;

            } else {
                break;
            }
        }
        return count;
    }

    public String RemovingSpacesFromName(String input) {
        String regex = "GCS_|[-_ /\\\\[\\\\]()]";
        Pattern pattern = Pattern.compile(regex);
        Matcher match = pattern.matcher(input);
        String updatedName = match.replaceAll("");
        updatedName = updatedName.replaceAll("\\s+", "");
        return updatedName.toLowerCase();
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
