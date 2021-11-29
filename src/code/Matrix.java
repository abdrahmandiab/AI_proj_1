package code;

import java.util.*;

import static java.lang.Math.ceil;

//TODO add penalty on hostage death.
public class Matrix {
    public static int nodesExpanded = 0;
        public static String genGrid()
        {
            //Randomize generated objects numbers
            Random rng = new Random();
            int gridWidth = rng.nextInt(11)+5;
            int gridHeight = rng.nextInt(11)+5;
            int maxSpawns = gridHeight * gridWidth;
            ArrayList<Tuple> Locations = new ArrayList<Tuple>(); //Holds locations of every object to avoid overlapping

            int hostageNumber = 10000;
            int agentNumber = 10000;
            int pillNumber = 10000;
            int padNumber = 10000;

            while((hostageNumber + agentNumber + pillNumber + padNumber*2) > maxSpawns-2){
                hostageNumber = rng.nextInt(8) + 3;
                agentNumber = rng.nextInt(maxSpawns);
                pillNumber = rng.nextInt(hostageNumber+1)+1;
                padNumber = rng.nextInt(maxSpawns)/2;
            }


            //Start generating locations of objects

            //Neo
            Tuple neo = new Tuple(rng.nextInt(gridWidth),rng.nextInt(gridHeight));
            Locations.add(neo);
            //Telephone Booth
            Tuple tel = new Tuple(rng.nextInt(gridWidth),rng.nextInt(gridHeight));
            //Ensure TB is not at the same location as Neo el fager
            while(Locations.contains(tel))
                tel = new Tuple(rng.nextInt(gridWidth),rng.nextInt(gridHeight));
            Locations.add(tel);

            //Carry capacity of Neo
            int c = rng.nextInt(4)+1;

            //Start building the output string representing the grid
            String out = "";
            out = gridWidth + "," + gridHeight + ";"
                    + c + ";"
                    + neo.x + "," + neo.y + ";"
                    + tel.x + "," + tel.y + ";";

            // Generate Agents and their locations
            Tuple[] agentLocation = new Tuple[agentNumber];
            for (int i = 0; i < agentNumber; i++) {
                Tuple agent = new Tuple(rng.nextInt(gridWidth), rng.nextInt(gridHeight));
                while (Locations.contains(agent))
                    agent = new Tuple(rng.nextInt(gridWidth), rng.nextInt(gridHeight));
                Locations.add(agent);
                agentLocation[i] = agent;
                out += agent.x + "," + agent.y + ",";
            }
            out = out.substring(0, out.length() - 1);
            out += ";";

            // We need to define number of hostages now as pills depend on it
            // Generate pills and their locations
            Tuple[] pillLocation = new Tuple[pillNumber];
            for (int i = 0; i < pillNumber; i++) {
                Tuple pill = new Tuple(rng.nextInt(gridWidth), rng.nextInt(gridHeight));
                while (Locations.contains(pill))
                    pill = new Tuple(rng.nextInt(gridWidth), rng.nextInt(gridHeight));
                Locations.add(pill);
                pillLocation[i] = pill;
                out += pill.x + "," + pill.y + ",";
            }
            out = out.substring(0, out.length() - 1);
            out += ";";

            //Generate pads and their locations
            Tuple[] padLocation = new Tuple [padNumber];
            Tuple[] pad2Location = new Tuple [padNumber];
            for (int i = 0; i<padNumber;i++)
            {
                Tuple pad = new Tuple(rng.nextInt(gridWidth),rng.nextInt(gridHeight));
                Tuple pad2 = new Tuple(rng.nextInt(gridWidth),rng.nextInt(gridHeight));
                while(Locations.contains(pad))
                    pad = new Tuple(rng.nextInt(gridWidth),rng.nextInt(gridHeight));
                while(Locations.contains(pad))
                    pad2 = new Tuple(rng.nextInt(gridWidth),rng.nextInt(gridHeight));
                Locations.add(pad);
                Locations.add(pad2);
                padLocation[i]=pad;
                pad2Location[i]=pad2;
                out += pad.x + "," + pad.y + "," + pad2.x + "," + pad2.y + "," + pad2.x + "," + pad2.y + "," + pad.x + "," + pad.y + ",";
            }
            out = out.substring(0,out.length()-1);
            out += ";";


            // Generate Hostages and their locations
            int[] hostageStartDmg = new int[hostageNumber];
            Tuple[] hostageLocation = new Tuple[hostageNumber];
            for (int i = 0; i < hostageNumber; i++) {
                Tuple hostage = new Tuple(rng.nextInt(gridWidth), rng.nextInt(gridHeight));
                hostageStartDmg[i] = rng.nextInt(98) + 1;
                while (Locations.contains(hostage))
                    hostage = new Tuple(rng.nextInt(gridWidth), rng.nextInt(gridHeight));
                Locations.add(hostage);
                hostageLocation[i] = hostage;
                out += hostage.x + "," + hostage.y + "," + hostageStartDmg[i] + ",";
            }
            out = out.substring(0, out.length() - 1);
            out += ";";

            return out;

        }
//        return "5,5;" +                         // 0 M,N
//                "2;" +                          // 1 C
//                "0,4;" +                        // 2 code.Neo x,y
//                "1,4;" +                        // 3 TB x,y
//                "0,1,1,1,2,1,3,1,3,3,3,4;" +    // 4 Agents (x,y) pairs
//                "1,0,2,4;" +                    // 5 pill1.x,pill1.y
//                "0,3,4,3,4,3,0,3;" +            // 6 pad1.x,pad1.y,pad2.x,pad2.y
//                "2,0,78";         // 7 Hostage.x,Hostage.y,Hostage.damage ...
//
////        //EASY MODE
////        return "5,5;" +                         // 0 M,N
////                "2;" +                          // 1 C
////                "0,4;" +                        // 2 code.Neo x,y
////                "1,4;" +                        // 3 TB x,y
////                "1,3;" +    // 4 Agents (x,y) pairs
////                "1,0,2,4;" +                    // 5 pill1.x,pill1.y
////                "0,3,4,3;" +            // 6 pad1.x,pad1.y,pad2.x,pad2.y
////                "0,3,30";         // 7 code.Hostage.x,code.Hostage.y,code.Hostage.damage ...
//    }


    public static String solve(String grid, String strat, boolean viz){
        HashSet <String> NodesTable= new HashSet<String>();
        boolean inTable = false;
        Deque<Node> actionQueue = new ArrayDeque<Node>(); //Double ended queue
        Comparator<Node> UCcomparator = new UCComparator();
        Comparator<Node> GRcomparator = new GRComparator();
        Comparator<Node> AScomparator = new ASComparator();

        PriorityQueue<Node> UCActionQueue = new PriorityQueue<Node>(100000,UCcomparator);
        PriorityQueue<Node> AStarActionQueue = new PriorityQueue<Node>(100000,AScomparator);
        PriorityQueue<Node> GreedyActionQueue = new PriorityQueue<Node>(100000,GRcomparator);
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
        UCActionQueue.add(initState);
        GreedyActionQueue.add(initState);
        AStarActionQueue.add(initState);
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
        while(!isSolved && !(actionQueue.isEmpty()) && !(UCActionQueue.isEmpty()) && !(GreedyActionQueue.isEmpty()) && !(AStarActionQueue.isEmpty())) {

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
                    String solution = makeStringOfMoves(popped);
                    if(viz){
                    ArrayList<Node> nodes =  getNodes(popped);
                    for(Node node : nodes){
                        solution = printGrid(node,m,n)+"\n"+solution;
                    }
                    }
                    System.out.println(solution);
                    return solution; }

                // Extract variables from the state we popped
                Neo neoPopped = popped.neo;
                int numDeathsThisTurn = 0;
                if(popped!=initState) {
                    numDeathsThisTurn = applyPoisonToHostages(neoPopped, popped.hostages, popped.turnedAgents);
                }else{
                    numDeathsThisTurn = 0;
                }
                ArrayList<Hostage> hostagesPopped = new ArrayList<Hostage>();
                for (Hostage h : popped.hostages) {
                    hostagesPopped.add(new Hostage(new Tuple((int) h.location.x, (int) h.location.y), 100 - h.hp));
                }
                ArrayList<Agent> spawnedAgentsPopped = popped.spawnedAgents;
                ArrayList<Agent> turnedAgentsPopped = popped.turnedAgents;
                ArrayList<Tuple> pillsPopped = popped.pills;
                //Apply passive stuff
//                int numDeathsThisTurn = 0;


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

                canMoveL = checkCanMoveL(neoPopped, spawnedAgentsAround, turnedAgentsAround, hostagesPopped);
                canMoveR = checkCanMoveR(neoPopped , spawnedAgentsAround, turnedAgentsAround, n, hostagesPopped);
                canMoveU = checkCanMoveU(neoPopped, spawnedAgentsAround, turnedAgentsAround, hostagesPopped);
                canMoveD = checkCanMoveD(neoPopped, spawnedAgentsAround, turnedAgentsAround, m, hostagesPopped);
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
                        heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoC,TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoC,TB);
                    }
                    Node CarryNode = new Node(neoC, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, h3, pads, pillsPopped, "carry", popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ carryCost, popped.nodeLevel+1);
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
                        heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }
                    else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }
                    Node DropNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "drop", popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ dropCost, popped.nodeLevel+1);
                    possibleStates.add(DropNode);
                }
                if (canFly) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Fly(neoM, pads);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }
                    Node FlyNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "fly", popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ flyCost, popped.nodeLevel+1);
                    possibleStates.add(FlyNode);
                }
                if (canMoveL) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Left", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }
                    Node LMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "left" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(LMoveNode);
                }
                if (canMoveR) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Right", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(turnedAgentsPopped,hostagesPopped, neoM, TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped, hostagesPopped,neoM, TB);
                    }
                    Node RMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "right" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(RMoveNode);
                }
                if (canMoveU) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Up", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }
                    Node UMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "up" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(UMoveNode);
                }
                if (canMoveD) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    Move(neoM, "Down", m, n);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }
                    else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoM,TB);
                    }
                    Node DMoveNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPopped, pads, pillsPopped, "down" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ moveCost, popped.nodeLevel+1);
                    possibleStates.add(DMoveNode);
                }
                if (canTake) {
                    Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                    ArrayList<Hostage> hostagesPilled = new ArrayList<Hostage>();
                    for(Hostage h : hostagesPopped){
                        Hostage h1 = new Hostage(new Tuple((int)h.location.x,(int)h.location.y),100-h.hp);
                        hostagesPilled.add(h1);
                    }
                    ArrayList<Tuple> pillsPilled = new ArrayList<Tuple>();
                    for(Tuple p : pillsPopped){
                        Tuple p1 = new Tuple((int)p.x,(int)p.y);
                        pillsPilled.add(p1);
                    }
                    Take(neoM, hostagesPilled, pillsPilled);
                    int heuristicValue = 0;
                    if(strat =="GR1" || strat =="AS1"){
                        heuristicValue = H1(turnedAgentsPopped,hostagesPilled,neoM,TB);
                    }
                    else if (strat =="GR2" || strat =="AS2"){
                        heuristicValue = H2(turnedAgentsPopped,hostagesPilled,neoM,TB);
                    }
                    Node DropNode = new Node(neoM, m, n, TB, spawnedAgentsPopped, turnedAgentsPopped, hostagesPilled, pads, pillsPilled, "takePill" , popped, popped.numKills, popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ takeCost, popped.nodeLevel+1);
                    possibleStates.add(DropNode);
                }
                if (neoPopped.hp > 20) {
                    if (turnedAgentsAround.size() != 0 && (hostageWithNeoInCell==null || hostageWithNeoInCell.hp>2)) { // (a)
                        ArrayList<Agent> allAgentsAround = new ArrayList<Agent>();
                        allAgentsAround.addAll(turnedAgentsAround);
                        allAgentsAround.addAll(spawnedAgentsAround);

                        Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                        ArrayList<Agent> turnedAgentsPoppedCopy = (ArrayList<Agent>) turnedAgentsPopped.clone();
                        ArrayList<Agent> spawnedAgentsPoppedCopy = (ArrayList<Agent>) spawnedAgentsPopped.clone();
                        Kill(neoM, allAgentsAround, turnedAgentsPoppedCopy, spawnedAgentsPoppedCopy);
                        int heuristicValue = 0;
                        if(strat =="GR1" || strat =="AS1"){
                            heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoM,TB);
                        }
                        else if (strat =="GR2" || strat =="AS2"){
                            heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoM,TB);
                        }
                        Node KillNode = new Node(neoM, m, n, TB, spawnedAgentsPoppedCopy, turnedAgentsPoppedCopy, hostagesPopped, pads, pillsPopped, "kill", popped, popped.numKills + allAgentsAround.size(), popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ goodKillCost, popped.nodeLevel+1);
                        possibleStates.add(KillNode);
                    }
                    if (spawnedAgentsAround.size() != 0 && turnedAgentsAround.size() == 0  && (hostageWithNeoInCell==null || hostageWithNeoInCell.hp>2)) { // (b)
                        Neo neoM = new Neo(neoPopped.maxCarry , neoPopped.hp, new Tuple((int) neoPopped.location.x, (int) neoPopped.location.y), neoPopped.hostagesCarried, neoPopped.currentlyCarrying);
                        ArrayList<Agent> turnedAgentsPoppedCopy = (ArrayList<Agent>) turnedAgentsPopped.clone();
                        ArrayList<Agent> spawnedAgentsPoppedCopy = (ArrayList<Agent>) spawnedAgentsPopped.clone();
                        Kill(neoM, spawnedAgentsAround,  turnedAgentsPoppedCopy, spawnedAgentsPoppedCopy);
                        int heuristicValue = 0;
                        if(strat =="GR1" || strat =="AS1"){
                            heuristicValue = H1(turnedAgentsPopped,hostagesPopped,neoM,TB);
                        }
                        else if (strat =="GR2" || strat =="AS2"){
                            heuristicValue = H2(turnedAgentsPopped,hostagesPopped,neoM,TB);
                        }
                        Node KillNode = new Node(neoM, m, n, TB, spawnedAgentsPoppedCopy, turnedAgentsPoppedCopy, hostagesPopped, pads, pillsPopped, "kill" , popped, popped.numKills + spawnedAgentsAround.size(), popped.numDeaths + numDeathsThisTurn, heuristicValue, popped.costSoFar+ numDeathsThisTurn*15000+ badKillCost, popped.nodeLevel+1);
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
        return (hostages.isEmpty() && neo.currentlyCarrying==0 && turnedAgents.isEmpty() && checkTB(neo,TB));
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
    public static boolean checkCanMoveL(Neo neo , ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround, ArrayList<Hostage> hostageArr){
        boolean agentLeft = false;
        boolean hostageDyingLeft = false;
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
        for(Hostage h: hostageArr){
            if ((int)h.location.y == (int)neo.location.y - 1 && h.hp<=2){
                hostageDyingLeft = true;
            }
        }
        return ((int)neo.location.y !=0 && !agentLeft && !hostageDyingLeft);
    }
    public static boolean checkCanMoveR(Neo neo, ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround,  int n, ArrayList<Hostage> hostageArr){
        boolean agentRight = false;
        boolean hostageDyingRight = false;
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
        for(Hostage h: hostageArr){
            if ((int)h.location.y == (int)neo.location.y + 1 && h.hp<=2){
                hostageDyingRight = true;
            }
        }
        return (((int)neo.location.y !=(n-1)) && !agentRight && !hostageDyingRight);
    }
    public static boolean checkCanMoveU(Neo neo , ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround, ArrayList<Hostage> hostageArr){
        boolean agentUp = false;
        boolean hostageDyingUp = false; //if there is a dying hostage up, set to true.
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
        for(Hostage h: hostageArr){
            if ((int)h.location.x == (int)neo.location.x - 1 && h.hp<=2){ // if hostage is up
                hostageDyingUp = true;
            }
        }
        return ((int)neo.location.x !=0 && !agentUp && !hostageDyingUp);
    }
    public static boolean checkCanMoveD(Neo neo, ArrayList<Agent> spawnedAgentsAround, ArrayList<Agent> turnedAgentsAround, int m, ArrayList<Hostage> hostageArr){
        boolean agentDown = false;
        boolean hostageDyingDown = false;
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
        for(Hostage h: hostageArr){
            if ((int)h.location.x == (int)neo.location.x + 1 && h.hp<=2){
                hostageDyingDown = true;
            }
        }
        return ((int)neo.location.x !=m-1 && !agentDown && !hostageDyingDown);
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
        ArrayList<Hostage> hostagesCarried2 = (ArrayList<Hostage>) neo.hostagesCarried.clone();
        for (Hostage h: hostagesCarried2){
            //if hostage being carried no need to make a new agent, just leave it carried on neo.

                boolean alive = h.poisonTrigger();
                if (!alive){
                    neo.hostagesCarried.remove(h);
                    numDeaths+=1;
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

    public static int H1(ArrayList<Agent> turnedAgents, ArrayList<Hostage> hostages,Neo neo, Tuple tb){
        int n = (checkTB(neo,tb) && hostages.isEmpty() && turnedAgents.isEmpty())?0:1;
        //System.out.println(turnedAgents.size()+ hostages.size() + neo.currentlyCarrying+n);
        //return turnedAgents.size()/4 + hostages.size()  + n;
        return (int) (ceil((turnedAgents.size()) / 4 + ((turnedAgents.size() % 4 == 0) ? 0 : 1))+ hostages.size()+ n);
    }
    public static int H2( ArrayList<Agent> turnedAgents,ArrayList<Hostage> hostages ,Neo neo, Tuple tb){
        int n = (checkTB(neo,tb) && hostages.isEmpty() && turnedAgents.isEmpty())?0:1;
        return hostages.size() + n;
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
    public static ArrayList<Node> getNodes(Node n){
        ArrayList<Node> parents = new ArrayList<Node>();

        Node currentNode = n;
        while(currentNode != null ){
            parents.add(currentNode);
            currentNode = currentNode.parent;
        }
        return parents;

    }
    public static String printGrid(Node node, int m , int n){
        //_______________________________
        //|     |     |     |     |     |
        //|(7,7)|H(23)|     |     |     |
        //|     |     |     |     |     |
        //|     |     |     |     |     |
        //|     |     |     |     |     |
        //_______________________________
        //("4,3,H23;", "4,5,N" )
        String contents = "";
        String matrix = "";
        contents+=  node.neo.location.x+ "," + node.neo.location.y + ",   N  ;";
        contents+=  node.TB.x+ "," + node.TB.y + ",  TB  ;";
        for(Agent a : node.spawnedAgents){
            contents+= a.location.x +","+a.location.y+",  A   ;";
        }
        for(Agent a : node.turnedAgents){
            contents+= a.location.x +","+a.location.y+",  TA ;";
        }
        for(Hostage h : node.hostages){
            contents+= h.location.x +","+h.location.y+", H("+(100-h.hp)+");";
        }
        for(Tuple[] PP : node.pads){
            contents+= PP[0].x +","+PP[0].y+",( "+PP[1].x+":"+PP[1].y+");";
        }
        for(Tuple p: node.pills){
            contents+= p.x +","+p.y+",  P   ;";
        }

        String [] strs = contents.split(";");
        //matrix+= node.spawnedAgents+"\n\n";
        matrix+= node.thisMove+"\n";
        for(int i= 0; i<m; i++){
            matrix += "-------";
        }
        matrix+="\n";
        for(int i=0; i<m; i++){
            for (int j =0; j<n; j++){
                String spaces = "|      |";
                boolean neoHere = false;
                for(int k=0; k< strs.length; k++){
                    String[] commaSplits = strs[k].split(",");
                    if(Integer.parseInt(commaSplits[0]) == i && Integer.parseInt(commaSplits[1]) == j ){
                        spaces= "|";
                        if(neoHere){
                            spaces+="N";
                        }
                        spaces+=commaSplits[2]+"|";
                        if(commaSplits[2].contains("N")){
                            neoHere=true;
                        }
                    }
                }
                matrix+=spaces;

            }
            matrix+="\n";
        }

        for(int i=0;i<m; i++){

        }
        for(int i= 0; i<m; i++){
            matrix += "-------";
        }
        matrix+= "\n \n";
        return matrix;
    }

    public static void main(String args[]){
        String grid = genGrid();
        String grid2 = "6,6;2;2,4;2,2;0,4,1,4,3,0,4,2;0,1,1,3;4,4,3,1,3,1,4,4;0,0,92,1,2,38";
        String grid11 = "9,9;2;8,0;3,5;0,1,0,3,1,0,1,1,1,2,0,7,1,8,3,8,6,1,6,5;0,6,2,8;8,1,4,5,4,5,8,1;0,0,95,0,2,98,0,8,94,2,5,13,2,6,39";

        String solution = solve(grid2, "AS2", true);

    }
    //up, up, up, up, up, up, right, kill, up, kill, up, kill, down, right, right, right, up, right, right, kill, takePill, right, kill, left, down, down, carry, left, carry, down, drop; 3; 8; 949770
    //up, up, up, up, up, up, right, kill, up, kill, up, kill, down, right, right, right, up, right, right, kill, takePill, right, kill, left, down, down, carry, left, carry, down, drop; 3; 8; 949770
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
        if (x.heuristicCost + x.costSoFar < y.heuristicCost +y.costSoFar) {
            return -1;
        }
        if (x.heuristicCost + x.costSoFar> y.heuristicCost +y.costSoFar) {
            return 1;
        }
        return 0;
    }
}