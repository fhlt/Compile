package logic;

/**
 *
 * @author LiangTao
 */
public abstract class Adder {
    /**
     * 半加器 
     * @return 
     */
    public static byte[] HalfAdder(byte a,byte b){
        Utils.validate(a,b);
        return new byte[]{LogicGates.AND(a, b),LogicGates.XOR(a, b)};
    }
    /**
     * 全加器
     * @return 
     */
    public static byte[] FullAdder(byte a,byte b,byte ci){
        Utils.validate(a,b);
        Utils.validate(ci);
        byte fs=LogicGates.XOR(a, b);
        byte sco=LogicGates.AND(fs, ci);
        return new byte[]{LogicGates.OR(sco, LogicGates.AND(a, b)),LogicGates.XOR(fs, ci)};
    }
    /**
     * 八位全加器
     * @param a
     * @param b
     * @return 
     */
    public static byte[] EightAdder(byte[] a,byte[] b){
        if(a==null || a.length!=8){
             throw new RuntimeException("非法参数"); 
        }
        if(b==null || b.length!=8){
            throw new RuntimeException("非法参数"); 
        }
        byte[] resu=new byte[8];
        byte[] temp=null;
        byte ci=0;
        for(int i=7;i>=0;i--){
            Utils.validate(a[i],b[i]);
            temp=FullAdder(a[i],b[i],ci);
            ci=temp[0];
            resu[i]=temp[1];
        }
        return resu;
    }
    
    
    
    public static void main(String[] args){
       Utils.print( Adder.EightAdder(new byte[]{0,0,0,0,1,1,0,1}, new byte[]{0,0,0,0,1,0,0,1}));
    }
}
