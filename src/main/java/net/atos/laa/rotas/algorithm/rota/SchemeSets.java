package net.atos.laa.rotas.algorithm.rota;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import javafx.util.Pair;
import net.atos.laa.rotas.algorithm.dataset.Solicitor;

public class SchemeSets {
    public ArrayList<Solicitor> solicitors;
    private HashMap<Integer, ArrayList<Solicitor>> firms;
    private HashMap<Integer, ArrayList<Solicitor>> schemeMembers;
    private HashMap<Integer, Integer> noSolsinFirms;

    public ArrayList<Integer> getFirmIDs() {
        return firmIDs;
    }

    private ArrayList<Integer> firmIDs;
    private ArrayList<Integer> schemeIDs;
    private int solIndex;
    private int allocs;
    private int totalSols;
    private int adjacentCols;


    public ColumnType getType() {
        return type;
    }

    private ColumnType type;
    private HashMap<Integer, Integer> allocations;

    public SchemeSets(ArrayList<Solicitor> solicitors, HashMap<Integer, Integer> allocations, ColumnType type, int adjacentCols) {
        this.solicitors = solicitors;
        this.solIndex = 0;
        this.allocs = 0;
        this.allocations = allocations;
        this.type = type;
        this.adjacentCols = adjacentCols;
        //printSols(solicitors);
        noSolsinFirms = new HashMap<>();
        //Collections.shuffle(solicitors);
        totalSols = solicitors.size();
        firms = new HashMap<>();
        firmIDs = new ArrayList<>();
        putSolsInFirms();
        schemeMembers = new HashMap<>();
        schemeIDs = new ArrayList<>();
        putSolsinSchemes();


    }


    public void putSolsinSchemes() {
        for (Solicitor sol : solicitors) {
            if (!schemeMembers.containsKey(sol.getSchemeId())) {
                schemeMembers.put(sol.getSchemeId(), new ArrayList<>());
                schemeIDs.add(sol.getSchemeId());
            }
            putSolInScheme(sol);
        }
    }

    public void putSolsInFirms() {
        for (Solicitor sol : solicitors) {
            int firmid = sol.getFirmId();
            if (!firms.containsKey(firmid)) {
                firms.put(firmid, new ArrayList<>());
                firmIDs.add(firmid);
            }
            if (noSolsinFirms.containsKey(firmid)) {
                noSolsinFirms.put(firmid, noSolsinFirms.get(firmid) + 1);
            } else {
                noSolsinFirms.put(firmid, 1);
            }
            putSolInFirm(sol);
        }
    }

    public void shuffleSols() {
        ArrayList<Solicitor> sols = new ArrayList<>();
        ArrayList<Solicitor> startingList = solicitors;
        for (int i = 0; i < totalSols; i++) {
            int id = pickScheme(startingList.size());
            Solicitor leastSol = leastAllocations(startingList, id);
            schemeMembers.get(id).remove(0);
            startingList.remove(leastSol);
            sols.add(leastSol);
        }
        solicitors = sols;

        putSolsinSchemes();
    }

    public Solicitor leastAllocations(ArrayList<Solicitor> sols, int schemeID) {
        int least = 10000;
        int solID = -1;
        for (Solicitor sol : sols) {
            if (sol.getSchemeId() != schemeID) {
                continue;
            }
            int id = sol.getSolicitorId();
            int alloc = allocations.get(id);
            if (alloc < least) {
                least = alloc;
                solID = id;
            }
        }
        for (Solicitor sol : sols) {
            if (sol.getSolicitorId() == solID) {
                return sol;
            }
        }
        return null;
    }

    public int pickScheme(int solsToAssign) {
        Random rand = new Random();
        float random = rand.nextFloat();
        int i = 0;
        float ratio = 0;
        while (true) {
            int id = schemeIDs.get(i);
            float schemeSize = schemeMembers.get(id).size();
            ratio = ratio + schemeSize / solsToAssign;
            if (random <= ratio) {
                return id;
            }
            i++;
        }
    }

    public void printSols(ArrayList<Solicitor> sols) {
        for (Solicitor sol : sols) {
            System.out.println(sol.getName());
        }
    }

    //
    public void equalDist() {
        Collections.shuffle(solicitors);
        HashMap<Integer, Pair<Integer, Integer>> pairs = ratio(noSolsinFirms);
        if(pairs.size()==1){
            return;
        }
        ArrayList<Solicitor> sols = new ArrayList<>();
        int id = -1;
        int totalsols = totalSols;
        int patternSize = PatternSize(pairs);
        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < adjacentCols; i++) {
            ids.add(-1);
        }
        int timeout = 0;
        while (totalsols != 0) {
            //System.out.println("QUEUE");
//            for (int i : ids) {
//                System.out.println(i);
//            }
            if(timeout>10){
                break;
            }
            ArrayList<Integer> largestFirm = new ArrayList<>();
            int largest = -1;
            for (int i : firmIDs) {
                if (pairs.get(i).getKey() > largest && i != ids.get(adjacentCols-1)) {
                    ids.remove(adjacentCols - 1);
                    ids.add(0, i);
                    id = i;
                    largestFirm.add(id);
                    largest = pairs.get(i).getKey();
                }
            }
            if (largest <= 0) {
                pairs = ratio(noSolsinFirms);
                continue;
            }

            Random rand = new Random();
            id = largestFirm.get(rand.nextInt(largestFirm.size()));

            for (Solicitor sol : solicitors) {
                if (!sols.contains(sol) && sol.getFirmId() == id) {
                    sols.add(sol);
                    totalsols--;
                    pairs.put(id, new Pair<>(pairs.get(id).getKey() - 1, pairs.get(id).getValue()));
                    timeout = 0;
                    break;
                    //printSols(sols);
                    //System.out.println(sols.size());
                }
                //System.out.println("Total sols: "+ totalsols + "Largest: " + largest);
            }
            timeout++;
            //System.out.println(largest);
        }
        int pattern = patternSize;
        for(Solicitor sol : solicitors){
            if(sols.size() == solicitors.size()){
                break;
            }
            if(!sols.contains(sol)){
                if(pattern>sols.size()){
                    sols.add(sol);
                }else {
                    sols.add(pattern - 1, sol);
                }
                pattern += patternSize;
                if (pattern >= solicitors.size())
                    pattern = patternSize;
            }
        }

        solicitors = sols;
    }

    private int PatternSize(HashMap<Integer, Pair<Integer, Integer>> pairs) {
        int total = 0;
        for (int i : firmIDs) {
            total += pairs.get(i).getKey();
        }
        return total;
    }

    public HashMap<Integer, Pair<Integer, Integer>> ratio(HashMap<Integer, Integer> solAmount) {
        HashMap<Integer, Pair<Integer, Integer>> pairs = new HashMap<>();
        int minIndex = Collections.min(solAmount.values());
        //System.out.println(minIndex);
        for (int id : firmIDs) {
            int i = solAmount.get(id);
            int r = i % minIndex;
            if (r == 0) {
                //System.out.println("Pair = "+i/minIndex);
                pairs.put(id, new Pair<>(i / minIndex, 0));
            } else {
                // System.out.println("Pair = "+(i-r)/minIndex + ", "+r);
                pairs.put(id, new Pair<>((i - r) / minIndex, r));
            }
        }
//        try {
//            Thread.sleep(300);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return pairs;
    }


    public void putSolInScheme(Solicitor sol) {
        ArrayList<Solicitor> sols = schemeMembers.get(sol.getSchemeId());
        sols.add(sol);
        schemeMembers.put(sol.getSchemeId(), sols);
    }

    public void putSolInFirm(Solicitor sol) {
        ArrayList<Solicitor> sols = firms.get(sol.getFirmId());
        sols.add(sol);
        firms.put(sol.getFirmId(), sols);
    }

    public void firmsInSet() {
        ArrayList<Integer> firms = new ArrayList<>();
        for (Solicitor sol : solicitors) {
            if (!firms.contains(sol.getFirmId())) {
                firms.add(sol.getFirmId());
            }
        }
        //noOfFirms = firms.size();
    }

    public Solicitor requestSolicitor() {
        Solicitor solicitor = solicitors.get(solIndex);
        // If at the last element, return to the first element
        if(allocs == solicitors.size() -1){
            allocs = 0;
            if(solicitors.size()%7==0){
                solIndex+=solicitors.size()/2-1;
            }
        } else {
            allocs ++;
        }
        if (solIndex >= solicitors.size() - 1) {
            solIndex -= solicitors.size() - 1;
//            solicitors = swapSolicitor(solicitors);
            //Collections.shuffle(solicitors);
        } else {
            solIndex++;
        }

        return solicitor;
    }

    private void swapSolicitors(){
        ArrayList<Solicitor> sols = new ArrayList<>();
        int size = solicitors.size();
        for(int i = size-1;i>=size/2;i++){
           sols.add(0,solicitors.get(i));
        }


    }

    public Solicitor peekSolicitor() {
        return solicitors.get(solIndex);
    }
}




