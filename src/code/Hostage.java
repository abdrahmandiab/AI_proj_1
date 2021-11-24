package code;

public class Hostage {
    boolean wasCarried = false;
    int hp = 100;
    boolean alive = true;
    Tuple location;

    public Hostage ( Tuple location, int damage){
        this.hp -= damage;
        this.location = location;
    }
    public boolean poisonTrigger(){
        if (alive){
            hp-=2;
            if(hp <=0){
                hp = 0;
                alive = false;
            }
        }
        return alive;
    }
    public void HealDamage(){
        if(alive){
            hp+=20;
            if (hp>100){
                hp = 100;
            }
        }
    }

    @Override
    public String toString() {
        return "code.Hostage{" +
                "wasCarried=" + wasCarried +
                ", hp=" + hp +
                ", alive=" + alive +
                ", location=" + location +
                '}';
    }
}
