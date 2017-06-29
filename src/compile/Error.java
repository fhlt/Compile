package compile;

/**
 * 错误信息
 */
public class Error {
	private int id ;		//错误序号
	private String info;	//错误信息
	private int line ;		//错误所在行
	private Words word;		//错误的单词
	//构造方法
	public Error(int id,String info, int line ,Words word){
		this.setId(id);
		this.setInfo(info);
		this.setLine(line);
		this.setWord(word);
	}
	public Error(int id2, String info2) {
		// TODO Auto-generated constructor stub
		this.setId(id);
		this.setInfo(info);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public Words getWord() {
		return word;
	}
	public void setWord(Words word) {
		this.word = word;
	}
}
