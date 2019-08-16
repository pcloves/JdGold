package Monitor;

public enum EMonitor
{
    Invalid("Invalid", null),
    MonitorShake("MonitorShake", MonitorShake.class),
    MonitorLimit("MonitorLimit", MonitorLimit.class),
    MonitorHistory("MonitorHistory", MonitorHistory.class),
    ;

    public final String name;
    public final Class<? extends IGoldMonitor> clazz;

    EMonitor(String name, Class<? extends IGoldMonitor> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public static EMonitor of(final String name)
    {
        for (EMonitor monitor : EMonitor.values())
        {
            if (monitor.name.equals(name)) {
                return monitor;
            }
        }


        return Invalid;
    }

}
