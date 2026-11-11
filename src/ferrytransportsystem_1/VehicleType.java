package ferrytransportsystem_1;

public enum VehicleType {
    CAR(1),
    MINIBUS(2),
    TRUCK(3);
 
    public final int capacity;
 
    VehicleType(int capacity) {
        this.capacity = capacity;
    }
}
