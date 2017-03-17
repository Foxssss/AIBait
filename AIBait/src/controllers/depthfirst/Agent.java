package controllers.depthfirst;

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

    private Stack<StateObservation> soStack = new Stack<StateObservation>();
    private ArrayDeque<ACTIONS> actList = new ArrayDeque<ACTIONS>();
    private HashSet<StateObservation> actedSet = new HashSet<StateObservation>();
    public boolean samePosition(StateObservation state) {
    	for(StateObservation so : actedSet) {
    		if(state.equalPosition(so))
    			return true;
    	}
    	return false;
    }
    public void findPath(StateObservation stateObs) {
    	soStack.push(stateObs);
    	actList.addLast(stateObs.getAvailableActions().get(0));
    	actedSet.add(stateObs);
    	//find available action
    	while(!soStack.empty()) {
    		StateObservation lastState = soStack.pop();
    		ACTIONS lastAction = actList.pollLast();
    		ArrayList<ACTIONS> actions = lastState.getAvailableActions();
    		int index = actions.indexOf(lastAction);
    		while(index < actions.size()) {
	    		StateObservation stCopy = lastState.copy();
    			ACTIONS action = actions.get(index);
    			stCopy.advance(action);
    			if(samePosition(stCopy)) {
    				index++;
    			}
    			else if(stCopy.isGameOver()) {    				
    				if(stCopy.getGameWinner() == Types.WINNER.NO_WINNER) {
    				actedSet.add(stCopy);
    				index++;
    				}
    				else {
    					soStack.push(stateObs);
    					soStack.push(stCopy);
    					actList.addLast(action);
    					return;
    				}
    			}
    			else {
    				soStack.push(lastState);
    				actList.addLast(actions.get(index));
    				soStack.push(stCopy);
    				actList.addLast(stCopy.getAvailableActions().get(0));
    				actedSet.add(stCopy);
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
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
    	if(actList.isEmpty())
    		findPath(stateObs);
    	return actList.pollFirst();
    }
}
