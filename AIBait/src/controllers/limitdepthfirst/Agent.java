package controllers.limitdepthfirst;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractPlayer{
    /**
     * Observation grid.
     */
    protected ArrayList<Observation> grid[][];

    /**
     * block size
     */
    protected int block_size;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
    }
    
    private final int limitDepth = 3;
    private Stack<StateObservation> soStack = new Stack<StateObservation>();
    private ArrayDeque<ACTIONS> actList = new ArrayDeque<ACTIONS>();
    private HashSet<StateObservation> actedSet = new HashSet<StateObservation>();
	private HashSet<StateObservation> tryActedSet = new HashSet<StateObservation>();
	private ArrayDeque<ACTIONS> bestActList = new ArrayDeque<ACTIONS>(); // best action list
    
    public boolean samePosition(StateObservation state) {
    	for(StateObservation so : actedSet) {
    		if(state.equalPosition(so))
    			return true;
    	}
    	return false;
    }
    
    public void findPath(StateObservation stateObs) {
    	// confirm destination to be key or goal(portal)
    	Vector2d destination = null;
    	if(stateObs.getAvatarType() == 1) {
	    	ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
	    	Vector2d keypos = movingPositions[0].get(0).position; //钥匙的坐标
    		destination = keypos;
    	}
    	else if(stateObs.getAvatarType() == 4) {
	    	ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
	    	Vector2d goalpos = fixedPositions[1].get(0).position; //目标的坐标
    		destination = goalpos;
    	}
    	else
    		System.exit(-1);
    	
    	double minDist = -100000; // minimum distance to the destination
    	
    	soStack.push(stateObs);
    	actList.addLast(ACTIONS.ACTION_NIL);
    	actedSet.add(stateObs);
    	//tryActedSet.clear();
		//for(StateObservation stob : actedSet)
		//	tryActedSet.add(stob.copy());
    	while(!soStack.empty()) {
    		StateObservation lastState = soStack.pop();
    		ACTIONS lastAction = actList.pollLast();
    		ArrayList<ACTIONS> actions = lastState.getAvailableActions();
    		//if(soStack.empty()) {
    	    //	tryActedSet.clear();
    		//	for(StateObservation stob : actedSet)
    		//		tryActedSet.add(stob.copy());
    		//}
    		if(actList.size() == limitDepth) {
    			if(samePosition(lastState)) {
    				continue;
    			}
    			actedSet.add(lastState);
    			double dist = -Math.abs(lastState.getAvatarPosition().x - destination.x) - 
    					Math.abs(lastState.getAvatarPosition().y - destination.y);
    	        ArrayList<Observation>[] movingPositions = lastState.getMovablePositions();
    	        //if(movingPositions.length > 0)
    	        	for(int i = 0; i < movingPositions[movingPositions.length - 1].size(); ++i)
    	        		if(destination.equals(movingPositions[movingPositions.length - 1].get(i).position)) {
    	        			dist -= 1000;
    	        			break;
    	        		}
    			if(dist > minDist) {
    				minDist = dist;
    				bestActList = actList.clone();
    			}
    			continue;
    		}
    		int index = actions.indexOf(lastAction);
    		while(index < actions.size() - 1) {
    			//find available action
    			index++;
	    		StateObservation stCopy = lastState.copy();
    			ACTIONS action = actions.get(index);
    			stCopy.advance(action);
    			if(stCopy.equalPosition(lastState)) {
    				continue;
    			}
    			// get the key
    			if(stCopy.getAvatarPosition().equals(destination)) {
					soStack.push(lastState);
					//soStack.push(stCopy);
					actList.addLast(action);
					//tryActedSet.add(stCopy);
					// 比较这是否是取到钥匙的最短路径
					if(minDist != 0 || bestActList.size() > actList.size()) {
						minDist = 0;
						bestActList = actList.clone();
					}
    				break;
    			}
    			// lose or win
    			else if(stCopy.isGameOver()) {    				
    				if(stCopy.getGameWinner() == Types.WINNER.NO_WINNER) {
    					//tryActedSet.add(stCopy);
    					index++;
    				}
    				else {
    					soStack.push(lastState);
    					//soStack.push(stCopy);
    					actList.addLast(action);
    					//tryActedSet.add(stCopy);
    					// 比较这是否是到达目标的最短路径
    					if(minDist != 0 || bestActList.size() > actList.size()) {
    						minDist = 0;
    						bestActList = actList.clone();
    					}
    					break;
    				}
    			}
    			else {
    				soStack.push(lastState);
    				actList.addLast(actions.get(index));
    				soStack.push(stCopy);
    				actList.addLast(ACTIONS.ACTION_NIL);
    				//tryActedSet.add(stCopy);
    				break;
    			}
    		}
    	}
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    StateObservation tmp = null;
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	soStack.removeAllElements();
    	actList.clear();
    	bestActList.clear();
    	findPath(stateObs);
//    	StateObservation tmpso = stateObs.copy();
//    	tmpso.advance(bestActList.getFirst());
//    	actedSet.add(tmpso);
    	return bestActList.getFirst();
    }
}
