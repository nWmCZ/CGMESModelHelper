package eu.unicorn.helper.utils;

import eu.unicorn.cgmes.model.CgmesProfileType;

import java.util.*;

public enum Configuration {

    IGM_DEPENDENCY(populateIGMMap()),
    CGM_DEPENDENCY(populateCGMMap());

    Configuration(Map<CgmesProfileType, Set<CgmesProfileType>> igmDependencies) {
        this.igmDependencies = igmDependencies;
    }

    private Map<CgmesProfileType, Set<CgmesProfileType>> igmDependencies = populateIGMMap();
    private static Map<CgmesProfileType,Set<CgmesProfileType>> populateIGMMap() {
        Map<CgmesProfileType,Set<CgmesProfileType>> igmMap = new HashMap<>();
        // SV
        igmMap.put(CgmesProfileType.STATE_VARIABLES, EnumSet.of(
                CgmesProfileType.STEADY_STATE_HYPOTHESIS,
                CgmesProfileType.TOPOLOGY,
                CgmesProfileType.TOPOLOGY_BOUNDARY
        ));
        // TP
        igmMap.put(CgmesProfileType.TOPOLOGY, EnumSet.of(
                CgmesProfileType.EQUIPMENT
        ));
        // SSH
        igmMap.put(CgmesProfileType.STEADY_STATE_HYPOTHESIS, EnumSet.of(
                CgmesProfileType.EQUIPMENT
        ));
        // EQ
        igmMap.put(CgmesProfileType.EQUIPMENT, EnumSet.of(
                CgmesProfileType.EQUIPMENT_BOUNDARY
        ));
        // TP_BD
        igmMap.put(CgmesProfileType.TOPOLOGY_BOUNDARY, EnumSet.of(
                CgmesProfileType.EQUIPMENT_BOUNDARY
        ));
        // EQ_BD is not dependentOn anything
        igmMap.put(CgmesProfileType.EQUIPMENT_BOUNDARY,
                Collections.emptySet()
        );

        return igmMap;
    }

    private Map<CgmesProfileType, Set<CgmesProfileType>> cgmDependencies = populateCGMMap();

    private static Map<CgmesProfileType,Set<CgmesProfileType>> populateCGMMap() {
        // TODO create pattern for CGM
        return new HashMap<>();
    }

}
