package tu.tracking.system.models;

import java.util.GregorianCalendar;

/**
 * Created by Liza on 21.6.2016 г..
 */
public class TargetModel {
    private int id;
    private String type;
    private String name;
    private boolean isActive;
    private boolean shouldNotMove;
    private GregorianCalendar shouldNotMoveUntil;

    public TargetModel(int id, String type, String name, boolean isActive, boolean shouldNotMove, GregorianCalendar shouldNotMoveUntil){
        this.id = id;
        this.type = type;
        this.name = name;
        this.setIsActive(isActive);
        this.setShouldNotMove(shouldNotMove);
        this.shouldNotMoveUntil = shouldNotMoveUntil;
    }

    public int getId(){
        return this.id;
    }

    public String getType(){
        return this.type;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setType(String type){
        this.type = type;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public boolean getShouldNotMove() {
        return shouldNotMove;
    }

    public void setShouldNotMove(boolean shouldNotMove) {
        this.shouldNotMove = shouldNotMove;
    }

    public GregorianCalendar getShouldNotMoveUntil() {
        return shouldNotMoveUntil;
    }

    public void setShouldNotMoveUntil(GregorianCalendar shouldNotMoveUntil) {
        this.shouldNotMoveUntil = shouldNotMoveUntil;
    }
}
