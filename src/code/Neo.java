package code;

import java.util.ArrayList;

public class Neo {
    int hp = 100;
    public Tuple location;
    int maxCarry;
    int currentlyCarrying = 0;
    ArrayList<Hostage> hostagesCarried ;
    //[1,2,3] if 2 dies,

    public Neo(int maxCarry, int hp, Tuple location ,ArrayList<Hostage> hostagesCarried, int currentlyCarrying){
        this.maxCarry = maxCarry;
        this.hp = hp;
        this.location = location;
        this.hostagesCarried = new ArrayList<Hostage>();
        for(Hostage h: hostagesCarried){
            this.hostagesCarried.add(new Hostage(new Tuple((int)h.location.x,(int)h.location.y), 100-h.hp));
        }
        this.currentlyCarrying = currentlyCarrying;
    }
    public void Attack(){
        hp-=20;
    }

    public void HealDamage(){
        hp+=20;
        if (hp>100){
            hp = 100;
        }
    }

    @Override
    public String toString() {
        return "code.Neo{" +
                "hp=" + hp +
                ", location=" + location +
                ", maxCarry=" + maxCarry +
                ", currentlyCarrying=" + currentlyCarrying +
                '}';
    }
}
