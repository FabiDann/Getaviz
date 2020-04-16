package org.getaviz.generator.abap.city.steps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.getaviz.generator.abap.city.layouts.ACityBuildingLayout;
import org.getaviz.generator.abap.city.layouts.ACityDistrictLayout;
import org.getaviz.generator.abap.city.repository.ACityElement;
import org.getaviz.generator.abap.city.repository.ACityRepository;

import java.util.*;

public class ACityLayouter {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityRepository repository;

    public ACityLayouter(ACityRepository aCityRepository,SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
    }


    public void layoutRepository(){

        Collection<ACityElement> buildings = repository.getElementsByType(ACityElement.ACityType.Building);
        for (ACityElement building: buildings) {
            layoutBuilding(building);
        }

        layoutParentDistricts(buildings);
    }

    private void layoutParentDistricts(Collection<ACityElement> districtElements) {

        Collection<ACityElement> parentDistricts = getParentDistricts(districtElements);

        if (parentDistricts.isEmpty()){
            layoutVirtualRootDistrict(districtElements);
            return;
        }

        for(ACityElement parentDistrict : parentDistricts){
            layoutDistrict(parentDistrict);
        }

        layoutParentDistricts(parentDistricts);
    }


    private Collection<ACityElement> getParentDistricts(Collection<ACityElement> elements) {
        Map<String, ACityElement> parentDistricts = new HashMap<>();
        for(ACityElement element : elements){

            ACityElement parentElement = element.getParentElement();
            if(parentElement == null){
                continue;
            }

            String hash = parentElement.getHash();
            if(!parentDistricts.containsKey(hash)){
                parentDistricts.put(hash, parentElement);
            }
        }
        return parentDistricts.values();
    }

    private void layoutBuilding(ACityElement building) {
        Collection<ACityElement> floors = building.getSubElementsOfType(ACityElement.ACityType.Floor);
        Collection<ACityElement> chimneys = building.getSubElementsOfType(ACityElement.ACityType.Chimney);

        ACityBuildingLayout buildingLayout = new ACityBuildingLayout(building, floors, chimneys, config);
        buildingLayout.calculate();
    }

    private void layoutDistrict(ACityElement district) {
        Collection<ACityElement> subElements = district.getSubElements();

        ACityDistrictLayout aCityDistrictLayout = new ACityDistrictLayout(district,  subElements, config);
        aCityDistrictLayout.calculate();
    }

    private void layoutVirtualRootDistrict(Collection<ACityElement> districts){
        ACityElement virtualRootDistrict = new ACityElement(ACityElement.ACityType.District);

        ACityDistrictLayout aCityDistrictLayout = new ACityDistrictLayout(virtualRootDistrict,  districts, config);
        aCityDistrictLayout.calculate();
    }

}