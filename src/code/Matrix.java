package code;

import java.util.*;

//TODO heuristic function number 1 (city block distance, with infinite maxCarry)
// takes in the hostages, wl TB, w code.Neo, wl turnedAgents

public class Matrix {
    public static int nodesExpanded = 0;
    public static String genGrid(){
        return "5,5;" +                         // 0 M,N
                "2;" +                          // 1 C
                "0,4;" +                        // 2 code.Neo x,y
                "1,4;" +                        // 3 TB x,y
                "0,1,1,1,2,1,3,1,3,3,3,4;" +    // 4 Agents (x,y) pairs
                "1,0,2,4;" +                    // 5 pill1.x,pill1.y
                "0,3,4,3,4,3,0,3;" +            // 6 pad1.x,pad1.y,pad2.x,pad2.y
                "2,0,78";         // 7 Hostage.x,Hostage.y,Hostage.damage ...

        //EASY MODE
//        return "5,5;" +                         // 0 M,N
//                "2;" +                          // 1 C
//                "0,4;" +                        // 2 code.Neo x,y
//                "1,4;" +                        // 3 TB x,y
//                "0,0;" +    // 4 Agents (x,y) pairs
//                "1,0,2,4;" +                    // 5 pill1.x,pill1.y
//                "0,3,4,3;" +            // 6 pad1.x,pad1.y,pad2.x,pad2.y
//                "0,3,30";         // 7 code.Hostage.x,code.Hostage.y,code.Hostage.damage ...
    }


    public static String solve(String grid, String strat, boolean viz){
        HashSet <String> NodesTable= new HashSet<String>();
        boolean inTable = false;
        Deque<Node> actionQueue = new ArrayDeque<Node>(); //Double ended queue
        Comparator<Node> UCcomparator = new UCComparator();
        Comparator<Node> GRcomparator = new GRComparator();
        Comparator<Node> AScomparator = new ASComparator();

        PriorityQueue<Node> UCActionQueue = new PriorityQueue<Node>(100000,UCcomparator);
        PriorityQueue<Node> AStarActionQueue = new PriorityQueue<Node>(100000,GRcomparator);
        PriorityQueue<Node> GreedyActionQueue = new PriorityQueue<Node>(100000,AScomparator);
        // ^^
        boolean isSolved = false;
        String [] arr = grid.split(";");
        //Get m & n
        String [] mn = arr[0].split(",");
        int m = Integer.parseInt(mn[0]);
        int n = Integer.parseInt(mn[1]);
        Neo neo = parseNeo(arr[1], arr[2]);
        Tuple TB = parseTB(arr[3]);
        ArrayList<Agent> spawnedAgents = parseAgents(arr[4]);
        ArrayList<Tuple> pills = parsePills(arr[5]);
        Tuple[][] pads = parsePads(arr[6]); // pads[0][1] is a code.Tuple value with the coordinates of the second pad from the first pair
        ArrayList<Hostage> hostages = parseHostages(arr[7]);
        Node initState = new Node(neo, m, n,TB,spawnedAgents,new ArrayList<Agent>(), hostages, pads, pills, "init", null, 0 ,0,1000,0, 0);

        actionQueue.add(initState);
        boolean canMoveL = false;
        boolean canMoveR= false;
        boolean canMoveU= false;
        boolean canMoveD= false;
        boolean canFly= false;
        boolean canTake= false;
        boolean canDrop= false;
        ArrayList<Agent> spawnedAgentsAround = new ArrayList<Agent>();
        ArrayList<Agent> turnedAgentsAround = new ArrayList<Agent>();
        Hostage hostageWithNeoInCell = null;
        ArrayList<Node> possibleStates = new ArrayList<Node>();
        String states = "";
        int looper = 0;
        while(!isSolved && !(actionQueue.isEmpty())) {

            //FIXME USE THIS LOOP FOR DEBUGGING (delete when done)

//        // Loop while it is not solved, and the queue is not empty.
//
            //while(looper<100){
            looper++;
//            System.out.println("======== ITERATION "+looper+"========");
//            System.out.println();
//
//            for(code.Node n1 : actionQueue){ System.out.println(n1);System.out.println();}
//
//            for(code.Node n1 : actionQueue){ states += " "+n1.thisMove;}
//            System.out.println("states in queue: " + states);
            // states = "";


            //We have a Queue.
//            from it:
//            - When a node is dequeued from it, we must perform a goalTest on it first.
//              --> If the goal test is passed, then isSolved becomes true and we are done solving.
//              --> If it fails then we must dequeue it and generate possible states from it
//            - We generate possible future states (Nodes) from each action that is possible.
//              --> E.g. At this state I can move Left or Down or Kill or Fly.
//            - Based on the search algorithm, we choose the order which we enqueue these Nodes according to.
            // Pop & do Goal Test.
            inTable = false;
            possibleStates = new ArrayList<Node>();
            Node popped = null;
            if(strat == "DF" || strat =="BF" || strat =="ID"){
                popped = actionQueue.remove();
            }else if(strat == "UC"){
                popped = UCActionQueue.remove();
            }else if(strat == "GR1" || strat == "GR2"){
                popped = GreedyActionQueue.remove();
            }
            else{
                popped = AStarActionQueue.remove();
            }

            if (NodesTable.contains(popped.toString2())) {
                inTable = true;
            }
            else{
                NodesTable.add(popped.toString2() );
            }
            if (!inTable) {
                nodesExpanded += 1;
//                if (nodesExpanded % 1000 == 0) {
//                    System.out.println("expanded node #" + nodesExpanded +" actionQueue: "+ actionQueue.size() + " hashTableSize: "+ NodesTable.size());
//                }
                isSolved = goalTest(popped.hostages, popped.turnedAgents, popped.neo,popped.TB);
                //System.out.println("Solved: " + isSolved + popped.hostages + popped.turnedAgents + popped.neo.hostagesCarried + ", action: " + popped.thisMove + ", neo-location: " + popped.neo.location + " TB: (" + TB.x + "," + TB.y + ")");
                if (isSolved) {
                    return makeStringOfMoves(popped);
                }

                // Extract variables from the state we popped
                Neo neoPopped = popped.neo;
                ArrayList<Hostage> hostagesPopped = new ArrayList<Hostage>();
                for (Hostage h : popped.hostages) {
                    hostagesPopped.add(new Hostage(new Tuple((int) h.location.x, (int) h.location.y), 100 - h.hp));
                }
                ArrayList<Agent> spawnedAgentsPopped = popped.spawnedAgents;
                ArrayList<Agent> turnedAgentsPopped = popped.turnedAgents;
                ArrayList<Tuple> pillsPopped = popped.pills;
                //Apply passive stuff
                int numDeathsThisTurn = 0;
                if(popped!=initState) {
                    numDeathsThisTurn = applyPoisonToHostages(neoPopped, hostagesPopped, turnedAgentsPopped);
                }else{
                    numDeathsThisTurn = 0;
                }

                //LOW PRIORITY
                int badKillCost = 100;
                //MEDIUM PRIORITY
                int moveCost = 5;
                int flyCost = 3;
                int takeCost = 3;
                //HIGH PRIORITY
                int dropCost = 1;
                int carryCost = 1;
                int goodKillCost =2;
                // Check which moves are possible.
                canFly = checkFly(neoPopped, pads);
                hostageWithNeoInCell = checkCarry(neoPopped, hostagesPopped);
                canDrop = checkDrop(neoPopped, checkTB(neoPopped, TB));
                canTake = checkPill(neoPopped, pillsPopped);
                spawnedAgentsAround = checkAgentsNearby(neoPopped, spawnedAgentsPopped);
                turnedAgentsAround = checkAgentsNearby(neoPopped, turnedAgentsPopped);

                canMoveL = checkCanMoveL(neoPopped, spawnedAgentsAround, turnedAgentsAround);
                canMoveR = checkCanMoveR(neoPopped , spawnedAgentsAround, turnedAgentsAround, n);
                canMoveU = checkCanMoveU(neoPopped, spawnedAgentsAround, turnedAgentsAround);
                canMoveD = checkCanMoveD(neoPopped, spawnedAgentsAround, turnedAgentsAround, m);
                //System.out.println(neoPopped.location+ " canMoveL "+ canMoveL + " canMoveR " + canMoveR + " canMoveU "
                //        +canMoveU + " canMoveD " + canMoveD + " canFly " + canFly + " canDrop " + canDrop + " canTake: "+ canTake );


                // Be very careful with ordering of this next part, since that don't use cost/heuristic use this order.
                // The order Right now is <moveL, moveR, moveU, moveD, Fly, Carry, Drop,Take, Kill>
                // moveL moveR moveU moveD should have similar cost
                // only ONE of (Carry, Drop, Take) can be available at one time.
                //      i.e. there can't be a (hostage and pill) in same cell or (pill and TB) or (code.Hostage and TB).
                // For the kill action, we have 2 different scenarios (and that is if neo's hp > 20)
                //      a) Some agents around were hostages that turned + maybe natural spawns. (in this case give low cost since we must kill them)
                //      b) all agents around were spawned with grid. (in this case give kill high cost since he probably shouldn't kill them)
                //      It is either a or b or neither, but we can't have both since that's 2 kill action nodes.

                //Carry
                if (hostageWithNeoInCell != null) {
                    Neo neoC = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    ArrayList<Hostage> h3 = Carry(neoC, hostageWithNeoInCell, hostagesPopped);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoC, hostagesPopped, turnedAgentsPopped, TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoC, hostagesPopped, turnedAgentsPopped);
                    }
                    Node CarryNode = new Node(neoC, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, h3, pads, pillsPopped, "carry", popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ carryCost, popped.nodeLevel+1);
                    possibleStates.add(CarryNode);
                }
                if (canDrop) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                   // code.Hostage hostageToDrop = neoM.hostagesCarried.get(0);
//                    for (code.Hostage h : neoM.hostagesCarried) {
//                        if (h.hp < hostageToDrop.hp) {
//                            hostageToDrop = h;
//                        }
//                    }
                    Drop(neoM);

                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                    }
                    else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                    }
                    Node DropNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "drop", popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ dropCost, popped.nodeLevel+1);
                    possibleStates.add(DropNode);
                }
                if (canFly) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Fly(neoM, pads);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                    }
                    Node FlyNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "fly", popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ flyCost, popped.nodeLevel+1);
                    possibleStates.add(FlyNode);
                }
                if (canMoveL) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Left", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                    }
                    Node LMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "left" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(LMoveNode);
                }
                if (canMoveR) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Right", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                    }
                    Node RMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "right" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(RMoveNode);
                }
                if (canMoveU) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Up", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                    }
                    Node UMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "up" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(UMoveNode);
                }
                if (canMoveD) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Down", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                    }
                    else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                    }
                    Node DMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "down" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(DMoveNode);
                }
                if (canTake) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);

                    Take(neoM, hostagesPopped, pillsPopped);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                    }
                    else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                    }
                    Node DropNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "takePill" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ takeCost, popped.nodeLevel+1);
                    possibleStates.add(DropNode);
                }
                if (neoPopped.hp > 20) {
                    if (turnedAgentsAround.size() != 0) { // (a)
                        ArrayList<Agent> allAgentsAround = new ArrayList<Agent>();
                        allAgentsAround.addAll(turnedAgentsAround);
                        allAgentsAround.addAll(spawnedAgentsAround);

                        Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                        Kill(neoM, allAgentsAround, turnedAgentsPopped, spawnedAgentsPopped);
                        int heuristicValue = 0;
                        if(strat =="GR1" || strat =="AS1"){
                            heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                        }
                        else if (strat =="GR2" || strat =="AS2"){
                            heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                        }
                        Node KillNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "kill", popped, popped.numKills + allAgentsAround.size(), popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ goodKillCost, popped.nodeLevel+1);
                        possibleStates.add(KillNode);
                    }
                    if (spawnedAgentsAround.size() != 0 && turnedAgentsAround.size() == 0) { // (b)
                        Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                        Kill(neoM, spawnedAgentsAround,  turnedAgentsPopped, spawnedAgentsPopped);
                        int heuristicValue = 0;
                        if(strat =="GR1" || strat =="AS1"){
                            heuristicValue = H1(neoM, hostagesPopped, turnedAgentsPopped, TB);
                        }
                        else if (strat =="GR2" || strat =="AS2"){
                            heuristicValue = H2(neoM, hostagesPopped, turnedAgentsPopped);
                        }
                        Node KillNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "kill" , popped, popped.numKills + spawnedAgentsAround.size(), popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ badKillCost, popped.nodeLevel+1);
                        possibleStates.add(KillNode);
                    }
                }
                ArrayList<Node> ps2 = (ArrayList<Node>) possibleStates.clone();
                for (Node p: ps2){
                    if(NodesTable.contains(p.toString2())){
                        possibleStates.remove(p);
                    }
                }
                //TODO (Enqueue generated states in order based on start chosen)
                switch (strat) {
                    case "BF":
                        for (Node state : possibleStates) {
                            actionQueue.addLast(state);
                        }
                        break;
                    case "DF":
                        Collections.reverse(possibleStates);
                        for (Node state : possibleStates ) {
                            actionQueue.addFirst(state);
                        }
                        ;
                        break;
                    case "ID":
                        int currentLevel = 1;
                        int lastLevel;
                        if(possibleStates.isEmpty())
                            lastLevel = 0;
                        else
                            lastLevel = possibleStates.get(possibleStates.size()-1).getNodeLevel();
                        for(int i = 0; i < lastLevel; i++) {
                            for (Node state : possibleStates) {
                                if (state.getNodeLevel() > currentLevel)
                                    break;
                                actionQueue.addLast(state);
                            }
                            currentLevel++;
                        }
                        break;
                    case "UC":
                        for(Node state: possibleStates){
                            UCActionQueue.add(state);
                        }
                        break;
                    case "GR1":;
                    case "GR2":
                        for(Node state: possibleStates){
                            GreedyActionQueue.add(state);
                        }
                        ;
                        break;

                    case "AS1":;
                    case "AS2":
                        for(Node state: possibleStates){
                            AStarActionQueue.add(state);
                        }
                        ;
                        break;
                    default:
                        break;
                }
            }
        }
        return "No Solution";
    }

    //Checks
    public static boolean goalTest(ArrayList<Hostage> hostages, ArrayList<Agent> turnedAgents, Neo neo, Tuple TB){
        return (hostages.isEmpty() && neo.hostagesCarried.isEmpty()&& turnedAgents.isEmpty() && checkTB(neo,TB));
    }
    public static Hostage checkCarry(Neo neo, ArrayList<Hostage> hostages){
        for (Hostage h: hostages){
            if (((int)h.location.x == (int) neo.location.x) && ((int)h.location.y == (int) neo.location.y) && (neo.currentlyCarrying<neo.maxCarry)){
                return h;
            }
        }
        return null;
    }
    public static boolean checkTB(Neo neo, Tuple tb){
        if ((int)tb.x == (int) neo.location.x && (int)tb.y == (int) neo.location.y){
            return true;
        }
        return false;
    }
    public static boolean checkDrop(Neo neo, boolean atTB){
        if(!(neo.hostagesCarried.isEmpty()) && atTB){
            return true;
        }
        return false;
    }
    public static boolean checkPill(Neo neo, ArrayList<Tuple> pills){
        for (Tuple p : pills){
            if((int)p.x == (int) neo.location.x && (int)p.y == (int) neo.location.y){
                return true;
            }
        }
        return false;
    }
    public static ArrayList<Agent> checkAgentsNearby(Neo neo, ArrayList<Agent> agents ){
        ArrayList<Agent> agents2 = new ArrayList<Agent>();

        for (Agent a : agents){
            //This condition handles Up and Down (same x -> up & down)
            if (((int) a.location.y ==(int) neo.location.y) && (((int) a.location.x ==(int) neo.location.x-1) || ((int) a.location.x ==(int) neo.location.x+1))){
                agents2.add(a);
            }

            //This condition handles Left and Right (same x -> up & down)
            if (((int) a.location.x ==(int) neo.location.x) && (((int) a.location.y ==(int) neo.location.y-1) || ((int) a.location.y ==(int) neo.location.y+1))){
                agents2.add(a);
            }
        }
        return agents2;
    }
    public static boolean checkFly(Neo neo, Tuple[][] pads){
        for(int i = 0; i<pads.length; i++){
            if (((int)pads[i][0].x == (int) neo.location.x && (int)pads[i][0].y == (int) neo.location.y) ){
                return true;
            }
            if (((int)pads[i][1].x == (int) neo.location.x && (int)pads[i][1].y == (int) neo.location.y) ){
                return true;
            }
        }
        return false;
    }
    public static boolean checkCanMoveL(Neo neo , ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround){
        boolean agentLeft = false;
        for(Agent a: spawnedAgentsAround){
            if ((int)a.location.y == (int)neo.location.y - 1){
                agentLeft = true;
            }
        }
        for(Agent a: turnedAgentsAround){
            if ((int)a.location.y == (int)neo.location.y - 1){
                agentLeft = true;
            }
        }
        return ((int)neo.location.y !=0 && !agentLeft);
    }
    public static boolean checkCanMoveR(Neo neo, ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround,  int n){
        boolean agentRight = false;
        for(Agent a: spawnedAgentsAround){
            if ((int)a.location.y == (int)neo.location.y + 1){
                agentRight = true;
            }
        }
        for(Agent a: turnedAgentsAround){
            if ((int)a.location.y == (int)neo.location.y + 1){
                agentRight = true;
            }
        }
        return (((int)neo.location.y !=(n-1)) && !agentRight);
    }
    public static boolean checkCanMoveU(Neo neo , ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround){
        boolean agentUp = false;
        for(Agent a: spawnedAgentsAround){
            if ((int)a.location.x == (int)neo.location.x - 1){
                agentUp = true;
            }
        }
        for(Agent a: turnedAgentsAround){
            if ((int)a.location.x == (int)neo.location.x - 1){
                agentUp = true;
            }
        }
        return ((int)neo.location.x !=0 && !agentUp);
    }
    public static boolean checkCanMoveD(Neo neo, ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround, int m){
        boolean agentDown = false;
        for(Agent a: spawnedAgentsAround){
            if ((int)a.location.x == (int)neo.location.x + 1){
                agentDown = true;
            }
        }
        for(Agent a: turnedAgentsAround){
            if ((int)a.location.x == (int)neo.location.x + 1){
                agentDown = true;
            }
        }
        return ((int)neo.location.x !=m-1 && !agentDown);
    }

    //Parsing
    public static Neo parseNeo (String maxCarry1, String str){
        String [] arr = str.split(",");
        int xLoc = Integer.parseInt(arr[0]);
        int yLoc = Integer.parseInt(arr[1]);
        int maxCarry = Integer.parseInt(maxCarry1);
        Tuple location = new Tuple(xLoc, yLoc);

        Neo neo = new Neo(maxCarry,100, location, new ArrayList<Hostage>(), 0);
        return neo;
    }
    public static Tuple parseTB(String str){
        String [] str2 = str.split(",");
        int x = Integer.parseInt(str2[0]);
        int y = Integer.parseInt(str2[1]);
        return new Tuple(x,y);
    }
    public static ArrayList<Agent> parseAgents(String str){ // 0,1 ,1,1,2,1,3,1,3,3,3,4
        String [] str2 = str.split(","); // 12
        ArrayList<Agent> agents = new ArrayList<Agent>();
        for (int i=0; i<str2.length;i+=2){
            Tuple agentLoc = new Tuple(Integer.parseInt(str2[i]),Integer.parseInt(str2[i+1]));
            agents.add(new Agent(agentLoc)) ;
        }
        return agents;
    }
    public static ArrayList<Tuple> parsePills(String str){
        String [] str2 = str.split(",");
        ArrayList <Tuple>pills = new ArrayList<Tuple>();
        for (int i=0; i<str2.length;i+=2){
            pills.add( new Tuple(Integer.parseInt(str2[i]),Integer.parseInt(str2[i+1])));
        }

        return pills;
    }
    //FIXME pads are duplicated.
    public static Tuple[][] parsePads(String str){ //2D array of pads
        String [] str2 = str.split(",");
        Tuple [][] pads = new Tuple[str2.length/4][2]; // 1,2,3,4 , 3,4,1,2
                                                        // 1,2,3,4 [[1,2][3,4]]
        for (int i=0; i<str2.length;i+=4){
            pads[i/4][0] = new Tuple(Integer.parseInt(str2[i]),Integer.parseInt(str2[i+1]));
            pads[i/4][1] = new Tuple(Integer.parseInt(str2[i+2]),Integer.parseInt(str2[i+3]));
        }

        return pads;
    }
    public static ArrayList<Hostage> parseHostages(String str){
        String [] str2 = str.split(",");
        ArrayList<Hostage> hostages = new ArrayList<Hostage>();
        for (int i=0; i<str2.length;i+=3){
            Tuple hostLoc = new Tuple(Integer.parseInt(str2[i]),Integer.parseInt(str2[i+1]));
            hostages.add(new Hostage (hostLoc, Integer.parseInt(str2[i+2]) ));
        }

        return hostages;
    }
    //Passives
    public static int  applyPoisonToHostages(Neo neo, ArrayList<Hostage> hostages, ArrayList<Agent> turnedAgents){
        int numDeaths = 0;
        for (Hostage h: neo.hostagesCarried){
            //if hostage being carried no need to make a new agent, just leave it carried on neo.
            if(h.alive){
                boolean alive = h.poisonTrigger();
                if (!alive){
                    numDeaths +=1;
                }
            }
        }
        ArrayList<Hostage> hostages2 = (ArrayList<Hostage>)hostages.clone();
        for (Hostage h: hostages2){
            boolean alive = h.poisonTrigger();
            if(!alive){
                Agent a = new Agent(new Tuple((int) h.location.x,(int)h.location.y));
                turnedAgents.add(a);
                numDeaths+=1;
                hostages.remove(h);
            }
        }
        return numDeaths;
    }
    // Heuristic functions & helpers
    public static int cityBlockDist(Tuple t1, Tuple t2){
        int dist = 0;
        dist += Math.abs(((int)t1.x-(int)t2.x));
        dist += Math.abs(((int)t1.y-(int)t2.y));

        return dist;
    }

    //FIXME make a combined array of turnedAgents and hostages then find shortest pathing across them.
    public static int H1(Neo neo, ArrayList<Hostage> hostages, ArrayList<Agent> turnedAgents, Tuple tb){
    return 0;
//        int dist = 0;
//        int minSoFar = 99999;
//        ArrayList<Integer> distances= new ArrayList<Integer>();
//        ArrayList<code.Hostage> hostagesCopy = (ArrayList<code.Hostage>) hostages.clone();
//        code.Hostage ph = null;
//
//        for(code.Hostage h : hostages){
//            dist = cityBlockDist(neo.location, h.location);
//            if(dist< minSoFar){
//                minSoFar =  dist;
//                ph = h;
//            }
//        }
//        hostagesCopy.remove(ph);
//        distances.add(minSoFar);
//        code.Hostage ph2 = null;
//        while(distances.size()< hostages.size()){
//            minSoFar = 999999;
//            for(code.Hostage h : hostagesCopy){
//                dist = cityBlockDist(ph.location, h.location);
//                if(dist< minSoFar){
//                    minSoFar =  dist;
//                    ph2 = h;
//                }
//            }
//            distances.add(minSoFar);
//            hostagesCopy.remove(ph2);
//            ph = ph2;
//        }
//        code.Agent agento = null;
//        code.Agent phAgent = null;
//        ArrayList<code.Agent> agentsCopy = (ArrayList<code.Agent>) turnedAgents.clone();
//        while(distances.size()< hostages.size()+turnedAgents.size()){
//            minSoFar = 999999;
//            for(code.Agent a : agentsCopy){
//                dist = cityBlockDist(phAgent.location, a.location);
//                if(dist< minSoFar){
//                    minSoFar =  dist;
//                    agento = a;
//                }
//            }
//            distances.add(minSoFar);
//            agentsCopy.remove(agento);
//            phAgent = agento;
//        }
//        int tbdist = cityBlockDist(neo.location, tb);
//        int sumDistances = 0;
//        for(int i : distances){
//            sumDistances+= i;
//        }
//        return tbdist + sumDistances;
    }
    public static int H2(Neo neo, ArrayList<Hostage> hostages,ArrayList<Agent> turnedAgents){
        int counter = 0;
        for (Hostage h: hostages){
            counter++;
        }
        for(Agent a : turnedAgents){
            counter++;
        }
        return counter;
    }
    //Actions
    public static void Move(Neo neo, String direction, int m , int n){
        switch(direction){
            case "Up": if((int)neo.location.x !=0){
                neo.location = new Tuple((int) neo.location.x-1, (int) neo.location.y);
            };break;
            case "Down":if((int)neo.location.x !=m){
                neo.location = new Tuple((int) neo.location.x+1, (int) neo.location.y);
            } ;break;
            case "Left":if((int)neo.location.y !=0){
                neo.location = new Tuple((int) neo.location.x, (int) neo.location.y-1);
            } ;break;
            case "Right": if((int)neo.location.y !=n){
                neo.location = new Tuple((int) neo.location.x, (int) neo.location.y+1);
            };break;
            default: break;
        }
    }
    public static void Kill(Neo neo, ArrayList<Agent> agents, ArrayList<Agent> turnedAgents, ArrayList<Agent> spawnedAgents){
        neo.Attack();

        for(Agent a: agents){
            turnedAgents.remove(a);
            spawnedAgents.remove(a);
        }
    }
    public static void Drop(Neo neo){
        neo.hostagesCarried.clear();
        neo.currentlyCarrying=0;
        //hostSaved +=1;
    }
    public static void Fly(Neo neo, Tuple[][] pads){
        for(int i = 0; i<pads.length; i++){
            if (((int)pads[i][0].x == (int) neo.location.x && (int)pads[i][0].y == (int) neo.location.y) ){
                neo.location = new Tuple(pads[i][1].x, pads[i][1].y);

                break;
            }
//            if (((int)pads[i][1].x == (int) neo.location.x && (int)pads[i][1].y == (int) neo.location.y) ){
//                neo.location = new code.Tuple(pads[i][0].x, pads[i][0].y);
//                break;
//            }
        }
    }
    public static ArrayList<Hostage> Carry(Neo neoJ, Hostage hostage, ArrayList<Hostage> hostages){

        if (neoJ.currentlyCarrying<neoJ.maxCarry) {
            hostage.wasCarried = true;
            hostage.location = neoJ.location;
            neoJ.hostagesCarried.add(hostage);

            neoJ.currentlyCarrying += 1;
        }
        ArrayList<Hostage> hostages2 = new ArrayList<Hostage>();

        for (int i = 0; i<hostages.size(); i++){
            if(i != hostages.indexOf(hostage)) {
                hostages2.add(new Hostage(new Tuple((int) hostages.get(i).location.x, (int) hostages.get(i).location.y), 100 - hostages.get(i).hp));
            }
        }
        return hostages2;
    }
    public static void Take(Neo neo, ArrayList<Hostage> hostages , ArrayList<Tuple> pills){
        neo.HealDamage();
        for (Hostage h: hostages){
            h.HealDamage();
        }
        for (Hostage h: neo.hostagesCarried){
            h.HealDamage();
        }
        ArrayList<Tuple> pills2 = (ArrayList<Tuple>) pills.clone();
        for(Tuple p : pills2){
            if ((int)p.x == (int) neo.location.x && (int)p.y == (int) neo.location.y){
                pills.remove(p);
            }
        }
    }

    public static String makeStringOfMoves(Node n){
        Node currentNode = n;
        String result = "";
        while(currentNode != null ){
            result = currentNode.thisMove + ", " + result ;
            currentNode = currentNode.parent;
        }
        result = result.substring(5, result.length()-2);
        return result + "; " + n.numDeaths+"; " + n.numKills + "; " +nodesExpanded ;

    }

    public static void main(String args[]){
        String grid = genGrid();
        String solution = solve(grid, "BF", true);
        //System.out.println("hostages saved: "+ hostSaved);
        System.out.println(solution);
    }

}



class UCComparator implements Comparator<Node> {
    @Override
    public int compare(Node x, Node y) {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if (x.costSoFar < y.costSoFar) {
            return -1;
        }
        if (x.costSoFar > y.costSoFar) {
            return 1;
        }
        return 0;
    }
}
class GRComparator implements Comparator<Node> {
    @Override
    public int compare(Node x, Node y) {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if (x.heuristicCost < y.heuristicCost) {
            return -1;
        }
        if (x.heuristicCost > y.heuristicCost) {
            return 1;
        }
        return 0;
    }
}
class ASComparator implements Comparator<Node> {
    @Override
    public int compare(Node x, Node y) {
        // Assume neither string is null. Real code should
        // probably be more robust
        // You could also just return x.length() - y.length(),
        // which would be more efficient.
        if (x.costSoFar+x.heuristicCost < y.costSoFar + y.heuristicCost) {
            return -1;
        }
        if (x.costSoFar+x.heuristicCost > y.costSoFar+ y.heuristicCost) {
            return 1;
        }
        return 0;
    }
}