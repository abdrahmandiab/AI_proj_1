import java.util.Arrays;
import java.util.ArrayList;
public class Node {
    public Neo neo;
    public int m;
    public int n;
    public Tuple TB;
    public ArrayList<Agent> spawnedAgents = new ArrayList<Agent>();
    public ArrayList<Hostage> hostages = new ArrayList<Hostage>();
    public ArrayList<Agent> turnedAgents = new ArrayList<Agent>();
    public Tuple[][] pads;
    public ArrayList<Tuple> pills = new ArrayList<Tuple>();
    public Node parent;
    public String thisMove;
    public int numKills;
    public int numDeaths;
    public int heuristicCost;
    public int costSoFar;
    public int nodeLevel;

    //Each node represents a state.
    public Node(Neo neo, int m, int n, Tuple TB, ArrayList<Agent> spawnedAgents, ArrayList<Agent> turnedAgents, ArrayList<Hostage> hostages
    , Tuple[][] pads, ArrayList<Tuple> pills, String thisMove, Node parent, int numKills, int numDeaths , int heuristicCost, int costSoFar, int nodeLevel){
        //init state
//        this.neo = new Neo(neo.maxCarry, new Tuple((int)neo.location.x, (int) neo.location.y));
//        this.neo.hostagesCarried = (ArrayList<Hostage>) neo.hostagesCarried.clone();
        this.neo = neo;
        this.m = m;
        this.n = n;
        this.TB = TB;
        for(Agent a : spawnedAgents) {
            this.spawnedAgents.add(new Agent(new Tuple((int)a.location.x,(int) a.location.y)));
        }
        for (Hostage h: hostages){
            this.hostages.add(new Hostage(new Tuple((int)h.location.x, (int)h.location.y), 100-h.hp));
        }
        for(Agent a : turnedAgents) {
            this.turnedAgents.add(new Agent(new Tuple((int)a.location.x,(int) a.location.y)));
        }
        this.pads = pads; // pads[0][1].x ==> x coordinate, of second pad, in first pad pair.
        for(Tuple p : pills) {
            this.pills.add(new Tuple((int)p.x,(int) p.y));
        }
        this.thisMove = thisMove;
        this.parent = parent;
        this.numDeaths = numDeaths;
        this.numKills = numKills;
        this.heuristicCost = heuristicCost;
        this.costSoFar = costSoFar;
        this.nodeLevel = nodeLevel;
    }


    public String toString2() {
        return ("Node{" +
                "\nneo=" + neo +
                // "\n, TB=" + TB +
                "\n, spawnedAgents=" + spawnedAgents +
                "\n, hostages=" + getHostString(hostages) +
                "\n, turnedAgents=" + turnedAgents +
                // "\n, pads=" + Arrays.toString(pads) +
                // "\n, pills=" + pills +
                // "\n, thisMove="+ thisMove +
                '}');
    }
    public String getHostString(ArrayList<Hostage> hostages){
        String res = "";
        for (Hostage h : hostages){
            res+= "["+h.alive+"]";
        }
        return res;
    }
    @Override
    public String toString() {
        return ("Node{" +
                "\nneo=" + neo +
                "\n, TB=" + TB +
                "\n, spawnedAgents=" + spawnedAgents +
                "\n, hostages=" + hostages +
                "\n, turnedAgents=" + turnedAgents +
                "\n, pads=" + Arrays.toString(pads) +
                "\n, pills=" + pills +
                "\n, thisMove="+ thisMove +
                '}');
    }
}
