package logic;

/**
 *
 * @author LiangTao
 */
public abstract class LogicGates {
    /**
     * 逻辑与门电路
     * @param a
     * @param b
     * @return 
     */
    public static byte AND(byte a,byte b){
        if(a==1 && b==1){
            return 1;
        }
        return 0;
    }
    /**
     * 逻辑或门电路
     * @param a
     * @param b
     * @return 
     */
    public static byte OR(byte a,byte b){
        if(a==1 || b==1){
            return 1;
        }
        return 0;
    }
        /**
     * 逻辑或门电路
     * @param a
     * @param b
     * @return 
     */
    public static byte XOR(byte a,byte b){
        if(a==b){
            return 0;
        }
        return 1;
    }
    /**
     * 逻辑非门
     * @param b
     * @return 
     */
    public static byte NOT(byte b){
        if(b==1){
            return 0;
        }
        return 1;
    }
    
        /**
     * 逻辑与非门
     * @param b
     * @return 
     */
    public static byte NAND(byte a,byte b){
        if(b==1 && a==1){
            return 0;
        }
        return 1;
    }
     /**
     * 逻辑缓冲器
     * @param b
     * @return 
     */
    public static byte BUFFER(byte b){
        return b;
    }
    
    
     /**
     * 逻辑或非门
     * @param b
     * @return 
     */
    public static byte NOR(byte a,byte b){
        if(b==0 && a==0){
            return 1;
        }
        return 0;
    }
    

    
    public static void main(String[] args){
        byte a=1;
        byte b=   LogicGates.AND((byte)1, (byte)0);
    }
}

