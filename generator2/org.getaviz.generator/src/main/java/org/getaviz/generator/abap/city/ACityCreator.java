package org.getaviz.generator.abap.city;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.getaviz.generator.SettingsConfiguration;
import org.neo4j.driver.v1.types.Node;

import java.util.*;

public class ACityCreator {

    private Log log = LogFactory.getLog(this.getClass());
    private SettingsConfiguration config;

    private ACityRepository repository;

    public ACityCreator(ACityRepository aCityRepository,SettingsConfiguration config) {
        this.config = config;

        repository = aCityRepository;
    }



    public void createRepositoryFromNodeRepository(NodeRepository nodeRepository){

        createAllACityElements(nodeRepository);

        createAllACityRelations(nodeRepository);
    }

    private void createAllACityRelations(NodeRepository nodeRepository) {

        Collection<ACityElement> aCityElements = repository.getAllElements();

        for (ACityElement element: aCityElements){

            Node sourceNode = element.getSourceNode();
            Collection<ACityElement> childElements = getChildElementsBySourceNode(nodeRepository, sourceNode);

            if( element.getType() == ACityElement.ACityType.District){

                createTypeDistricts(element, childElements);

            } else {
                for (ACityElement childElement: childElements) {
                    element.addSubElement(childElement);
                    childElement.setParentElement(element);
                }
            }
        }

    }

    private void createTypeDistricts(ACityElement parentDistrict, Collection<ACityElement> childElements) {
        Map<String, ACityElement> typeDistrictMap = new HashMap<>();

        for (ACityElement childElement: childElements) {
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, "Class", SAPNodeLabels.Class);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, "Report", SAPNodeLabels.Report);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, "FunctionGroup",SAPNodeLabels.FunctionGroup);
            addChildToTypeDistrict(parentDistrict, childElement, typeDistrictMap, "Table", SAPNodeLabels.Table);

            //TODO DDIC TypeDistrict
        }
    }

    private void addChildToTypeDistrict(ACityElement parentDistrict, ACityElement childElement, Map<String, ACityElement> typeDistrictMap, String districtType, SAPNodeLabels sapNodeLabel) {

        Node childSourceNode = childElement.getSourceNode();

        if( childSourceNode.hasLabel( sapNodeLabel.name()) ){
            if( !typeDistrictMap.containsKey(districtType)){
                ACityElement typeDistrict = new ACityElement(ACityElement.ACityType.District);
                typeDistrictMap.put(districtType, typeDistrict);

                repository.addElement(typeDistrict);
                parentDistrict.addSubElement(typeDistrict);
                typeDistrict.setParentElement(parentDistrict);
            }

            ACityElement typeDistrict = typeDistrictMap.get(districtType);

            typeDistrict.addSubElement(childElement);
            childElement.setParentElement(typeDistrict);
        }

    }

    private Collection<ACityElement> getChildElementsBySourceNode(NodeRepository nodeRepository, Node node) {
        Collection<Node> childNodes = nodeRepository.getRelatedNodes(node, SAPRelationLabels.CONTAINS, true);
        if( childNodes.isEmpty()){
            return new TreeSet<>();
        }

        List<ACityElement> childElements = new ArrayList<>();
        for (Node childNode: childNodes ) {
            Long childNodeID = childNode.id();
            ACityElement childElement = repository.getElementBySourceID(childNodeID);
            childElements.add(childElement);
        }
        return childElements;
    }

    private ACityElement getParentElementBySourceNode(NodeRepository nodeRepository, Node node) {
        Collection<Node> parentNodes = nodeRepository.getRelatedNodes(node, SAPRelationLabels.CONTAINS, false);
        if(parentNodes.isEmpty()) {
            return null;
        }

        Node parentNode = parentNodes.iterator().next();
        Long parentNodeId = parentNode.id();

        ACityElement parentElement = repository.getElementBySourceID(parentNodeId);
        return parentElement;
    }


    private void createAllACityElements(NodeRepository nodeRepository) {
        createACityElementsFromSourceNodes(nodeRepository, SAPNodeLabels.Package, ACityElement.ACityType.District);

        createACityElementsFromSourceNodes(nodeRepository, SAPNodeLabels.Report, ACityElement.ACityType.Building);

        createACityElementsFromSourceNodes(nodeRepository, SAPNodeLabels.Class, ACityElement.ACityType.Building);

        createACityElementsFromSourceNodes(nodeRepository, SAPNodeLabels.FunctionGroup, ACityElement.ACityType.Building);

        createACityElementsFromSourceNodes(nodeRepository, SAPNodeLabels.Table, ACityElement.ACityType.Building);
    }

    private void createACityElementsFromSourceNodes(NodeRepository nodeRepository, SAPNodeLabels nodeLabel, ACityElement.ACityType aCityType) {
        Collection<Node> sourceNodes = nodeRepository.getNodesByLabel(nodeLabel);
        List<ACityElement> aCityElements = createACityElements(sourceNodes, aCityType);
        repository.addElements(aCityElements);
    }

    private List<ACityElement> createACityElements(Collection<Node> sourceNodes, ACityElement.ACityType aCityType) {
        List<ACityElement> aCityElements = new ArrayList<>();

        for( Node sourceNode: sourceNodes ) {
            ACityElement aCityElement = new ACityElement(aCityType);
            aCityElement.setSourceNode(sourceNode);
            aCityElements.add(aCityElement);
        }

        return aCityElements;
    }

}
