package logic;

/**
 *
 * @author LiangTao
 */
public abstract class Utils {
    
        
    /**
     * 
     * 只可以是0和1
     * @param a
     * @param b 
     */
    public static void validate(byte a,byte b){
        if((a!=0 && a!=1)|| (b!=0 && b!=1)){
           throw new RuntimeException("非法参数"); 
        }
    }
    
   
    
        /**
     * 
     * 只可以是0和1
     * @param a
     * @param b 
     */
    public static void validate(byte b){
        if( b!=0 && b!=1){
           throw new RuntimeException("非法参数"); 
        }
    }
    /**
     * 打印
     */
    public static void print(byte[] b){
        for(int i=0;i<b.length;i++){
            System.out.print(b[i]);
        }
        System.out.println();
    }
    /**
     * 字符串转化为16进制
     * @param s
     * @return
     */
    public static String toHexString(String s) 
	{ 
		String str=""; 
		for (int i=0;i<s.length();i++) 
		{ 
			int ch = (int)s.charAt(i); 
			String s4 = Integer.toHexString(ch); 
			str = str + s4; 
		} 
		return str; 
	} 
}
