package eu.unicorn.helper.model;

public enum CgmesProfileType {
    DOMAIN,
    EQUIPMENT_BOUNDARY,
    TOPOLOGY_BOUNDARY,
    EQUIPMENT,
    TOPOLOGY,
    STEADY_STATE_HYPOTHESIS,
    STATE_VARIABLES,
    DYNAMICS,
    GEOGRAPHICAL_LOCATION,
    DIAGRAM_LAYOUT;

    private CgmesProfileType() {
    }

    public static CgmesProfileType resolveProfile(String parsedProfile) {
        if (parsedProfile.contains("DiagramLayout")) {
            return CgmesProfileType.DIAGRAM_LAYOUT;
        } else if (parsedProfile.contains("Dynamics")) {
            return CgmesProfileType.DYNAMICS;
        } else if (parsedProfile.contains("EquipmentBoundary")) {
            return CgmesProfileType.EQUIPMENT_BOUNDARY;
        } else if (parsedProfile.contains("EquipmentCore") || parsedProfile.contains("EquipmentOperation") || parsedProfile.contains("EquipmentShortCircuit")) {
            return CgmesProfileType.EQUIPMENT;
        } else if (parsedProfile.contains("GeographicalLocation")) {
            return CgmesProfileType.GEOGRAPHICAL_LOCATION;
        } else if (parsedProfile.contains("StateVariables")) {
            return CgmesProfileType.STATE_VARIABLES;
        } else if (parsedProfile.contains("SteadyStateHypothesis")) {
            return CgmesProfileType.STEADY_STATE_HYPOTHESIS;
        } else if (parsedProfile.contains("TopologyBoundary")) {
            return CgmesProfileType.TOPOLOGY_BOUNDARY;
        } else if (parsedProfile.contains("Topology/")) { // It's correct to have "/" in name, to distinguish between TP and TP_BD
            return CgmesProfileType.TOPOLOGY;
        } else {
            System.out.println("Couldn't parse profile type: " + parsedProfile);
            System.exit(1);
            return null;
        }
    }
}