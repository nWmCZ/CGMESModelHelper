package eu.unicorn.helper.utils;

import eu.unicorn.cgmes.model.CgmesProfileType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DependencyResolver {

    private Map<CgmesProfileType,Set<CgmesProfileType>> igmMap = new HashMap<>();

    public  DependencyResolver() {
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
    }

    public static boolean resolveDependency(Map<CgmesProfileType, Set<CgmesProfileType>> currentDependency, Configuration configuration) {
        // TODO compare two maps
        // https://stackoverflow.com/questions/24814577/comparing-two-maps
        return false;
    }
}
