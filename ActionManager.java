import java.util.*;

public class ActionManager {
    private Stack<Action> actionHistory;
    private static final int MAX_ACTIONS = 20;
    
    public ActionManager() {
        this.actionHistory = new Stack<>();
    }
    
    public void executeAction(Action action) {
        // Execute the action
        action.execute();
        
        // Add to history
        actionHistory.push(action);
        
        // Limit history size
        if (actionHistory.size() > MAX_ACTIONS) {
            actionHistory.removeElementAt(0); // Remove oldest action
        }
    }
    
    public void undo() {
        if (!actionHistory.isEmpty()) {
            Action lastAction = actionHistory.pop();
            lastAction.undo();
        }
    }
    
    public boolean canUndo() {
        return !actionHistory.isEmpty();
    }
    
    public int getActionCount() {
        return actionHistory.size();
    }
    
    public void clearHistory() {
        actionHistory.clear();
    }
}

// Abstract base class for all actions
abstract class Action {
    public abstract void execute();
    public abstract void undo();
}

// Action for adding a waypoint
class AddWayPointAction extends Action {
    private MapPanel mapPanel;
    private WayPoint wayPoint;
    private List<WayPoint> previousState;
    
    public AddWayPointAction(MapPanel mapPanel, WayPoint wayPoint) {
        this.mapPanel = mapPanel;
        this.wayPoint = wayPoint;
        // Save current state before execution
        this.previousState = new ArrayList<>();
        for (WayPoint wp : mapPanel.getWayPoints()) {
            this.previousState.add(wp.clone());
        }
    }
    
    @Override
    public void execute() {
        mapPanel.addWayPointInternal(wayPoint);
    }
    
    @Override
    public void undo() {
        mapPanel.setWayPoints(previousState);
    }
}

// Action for resetting all waypoints
class ResetAction extends Action {
    private MapPanel mapPanel;
    private List<WayPoint> previousState;
    
    public ResetAction(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        // Save current state before execution
        this.previousState = new ArrayList<>();
        for (WayPoint wp : mapPanel.getWayPoints()) {
            this.previousState.add(wp.clone());
        }
    }
    
    @Override
    public void execute() {
        mapPanel.clearWayPoints();
    }
    
    @Override
    public void undo() {
        mapPanel.setWayPoints(previousState);
    }
}

// Action for inverting the route
class InvertAction extends Action {
    private MapPanel mapPanel;
    private List<WayPoint> previousState;
    
    public InvertAction(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
        // Save current state before execution
        this.previousState = new ArrayList<>();
        for (WayPoint wp : mapPanel.getWayPoints()) {
            this.previousState.add(wp.clone());
        }
    }
    
    @Override
    public void execute() {
        mapPanel.invertRoute();
    }
    
    @Override
    public void undo() {
        mapPanel.setWayPoints(previousState);
    }
}
