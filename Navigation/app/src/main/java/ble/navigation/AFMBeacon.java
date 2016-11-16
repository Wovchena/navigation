package ble.navigation;

public class AFMBeacon {
    String name;
    String adress;
    int major, minor, rssi;

    public AFMBeacon()
    {
        name=null;
        adress=null;
        major=0;
        minor=0;
        rssi=0;
    }
    public AFMBeacon(String n, String a, int maj, int min, int r)
    {
        name=n;
        adress=a;
        major=maj;
        minor=min;
        rssi=r;
    }

    public String getAdress()
    {
        return adress;
    }

    @Override
    public String toString()
    {
        return "Name: "+name+", Adress: "+adress+", Major: "+major+", Minor: "+minor+", RSSI: "+rssi;
    }
}
