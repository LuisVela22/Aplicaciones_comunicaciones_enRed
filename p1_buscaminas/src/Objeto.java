import java.io.Serializable;
class Objeto implements Serializable{
    int x;
	float y;
        String z;
        int [][] m;
       
	Objeto(int x, float y, String z,int[][] m){
		this.x = x;
		this.y = y;
                this.z = z;
                this.m = m;
	}

    public int getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getZ() {
        return z;
    }
    
    public int[][] getM(){
        return this.m.clone();
    }
               
        
	
	}