public class Agent {
    Tuple location;

    public Agent(Tuple location){
        this.location = location;
    }

    @Override
    public String toString() {
        return "Agent{ location=" + location +'}';
    }
}
