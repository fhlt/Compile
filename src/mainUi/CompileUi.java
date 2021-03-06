package mainUi;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.cert.Certificate;
import java.security.cert.PolicyQualifierInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import compile.ASM;
import compile.Error;
import compile.GrammarAnalysis;
import compile.LexAnalysis;
import compile.SemAnalysis;
import compile.Words;
import interpreter.Interpreter;
import nemu.Cpu;
import nemu.Exec;
import nemu.MyDOS;
/**
 * UI
 * @author Liangtao
 *
 */
public class CompileUi extends JFrame {
	private static final long serialVersionUID = -5418344602348249042L;
	private File currentFile;
	private JTextPane sourseFile;// 用来显示源文件的文本框
	private JTextArea consoleField;// 终端文本域
	private JTextPane tokenField;// token 串
	private JTextArea signField;// sign
	private JTextArea errField;// err

	private JDesktopPane desktop;
	private String soursePath;// 源文件路径
	private JSplitPane jSplitPane1;// 分割窗口
	private JSplitPane jSplitPane2;// 分割窗口
	private JScrollPane jScrollPane;
	private PopupMenu popupMenu;

	private JTabbedPane tabbedPane;
	private JLabel tokenLabel, signLabel, errLabel;
	private JScrollPane tokenPanel, signPanel, errPanel;
	private JToolBar bar1;// 工具栏1
	private JToolBar bar2;// 工具栏2

	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	private JTree tree;
	private Clipboard clipboard;// 剪贴板

	private String a[];
	private JList row;

	private GrammarAnalysis grammarAnalysis;
	private LexAnalysis lexAnalysis;
	private SemAnalysis semAnalysis;
	private ASM asm;
	
	public CompileUi() {
		this.init();
	}

	public void init() {
		currentFile = null;
		clipboard = this.getToolkit().getSystemClipboard();// 获取系统剪切板
		Toolkit toolkit = Toolkit.getDefaultToolkit();// 获取本机信息
		Dimension screen = toolkit.getScreenSize();
		setTitle("编译器");
		setSize(1500, 800);
		super.setResizable(true);// 允许生成窗体自由变化
		super.setLocation(screen.width / 2 - this.getWidth() / 2, screen.height / 2 - this.getHeight() / 2);

		root = new DefaultMutableTreeNode("Workspace");
		tree = new JTree(root);
		model = new DefaultTreeModel(root);
		desktop = new JDesktopPane();

		this.setContentPane(this.createContentPane());
		// 添加panel
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private void this_mousePressed(MouseEvent e) {
		int mods = e.getModifiers();
		// 鼠标右键
		if ((mods & InputEvent.BUTTON3_MASK) != 0) {
			// 弹出菜单
			popupMenu.show(sourseFile, e.getX(), e.getY());
		}
	}

	/*
	 * 右键菜单
	 */
	private void creatPopuMenu() {
		popupMenu = new PopupMenu();
		MenuItem item1 = new MenuItem();
		MenuItem item2 = new MenuItem();
		MenuItem item3 = new MenuItem();
		item1.setLabel("copy");
		item2.setLabel("cut");
		item3.setLabel("past");
		/*
		 * copy()
		 */
		item1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				copy();
			}
		});
		/*
		 * cut
		 */
		item2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				cut();
			}
		});
		/*
		 * past
		 */
		item3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				past();
			}
		});
		popupMenu.add(item1);
		popupMenu.add(item2);
		popupMenu.add(item3);
	}

	private JPanel createContentPane() {
		creatMenu();// 菜单栏
		creatPopuMenu();
		JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.NORTH, creatBottomPane());
		p.add(BorderLayout.CENTER, createcCenterPane());
		return p;
	}

	private void creatMenu() {
		JMenuBar jmb = new JMenuBar();
		JMenu jm1 = new JMenu("文件(F)");
		JMenu jm2 = new JMenu("编辑(E)");
		JMenu jm3 = new JMenu("词法分析(W)");
		JMenu jm4 = new JMenu("语法分析(P)");
		JMenu jm5 = new JMenu("中间代码生成(O)");
		JMenu jm6 = new JMenu("查看(V)");
		JMenu jm7 = new JMenu("帮助(H)");
		JMenuItem jmimportItem1 = new JMenuItem("复制");
		JMenuItem jmimportItem2 = new JMenuItem("文件另存为");
		JMenuItem jmimportItem3 = new JMenuItem("粘贴");
		JMenuItem jmimportItem4 = new JMenuItem("保存文件");
		JMenuItem jmimportItem5 = new JMenuItem("剪切");
		JMenuItem jmimportItem6 = new JMenuItem("使用帮助");

		JMenuItem jmimportItem7 = new JMenuItem("词法分析");
		JMenuItem jmimportItem8 = new JMenuItem("语法分析");
		JMenuItem jmimportItem9 = new JMenuItem("运行程序");

		JMenuItem jmimportItem = new JMenuItem("导入源文件");
		JMenuItem jmexitItem = new JMenuItem("退出程序");
		// 导入源文件
		jmimportItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				openFile();
			}
		});
		// 退出应用程序
		jmexitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);
			}
		});
		// 复制
		jmimportItem1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				copy();
			}
		});
		jmimportItem2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				try {
					SaveFile(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		// 粘贴
		jmimportItem3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				past();
			}
		});
		jmimportItem4.setMnemonic('S');//添加保存快捷键
		jmimportItem4.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.CTRL_MASK));
		jmimportItem4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(sourseFile.getText().equals("")){//如果文本域为空，不执行保存动作
					return;
				}
				try {
					SaveFile(currentFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		// 复制
		jmimportItem5.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				cut();
			}
		});
		// 词法分析
		jmimportItem7.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				lexAnaly();
			}
		});

		jm1.add(jmimportItem);
		jm1.add(jmexitItem);
		jm1.add(jmimportItem2);
		jm1.add(jmimportItem4);

		jm2.add(jmimportItem1);
		jm2.add(jmimportItem3);
		jm2.add(jmimportItem5);

		jm3.add(jmimportItem7);
		jm4.add(jmimportItem8);
		jm5.add(jmimportItem9);

		jmb.add(jm1);
		jmb.add(jm2);
		jmb.add(jm3);
		jmb.add(jm4);
		jmb.add(jm5);
		jmb.add(jm6);
		jmb.add(jm7);
		this.setJMenuBar(jmb);
	}

	/*
	 * 工具栏快捷按钮
	 */
	private Component creatBottomPane() {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		bar1 = new JToolBar();//编译工具栏
		bar2 = new JToolBar();//文件编辑工具栏

		JButton bt1 = new JButton("词法分析");
		JButton bt2 = new JButton("语法分析");
		JButton bt3 = new JButton("语义分析");
		JButton bt4 = new JButton("目标代码生成");
		JButton bt5 = new JButton("执行程序");
		// 词法分析
		bt1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				lexAnaly();
			}
		});
		//语法分析
		bt2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				parse();
			}
		});
		bt3.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//语义分析
				semAnalysis();
			}
		});
		bt4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//目标代码
				toASM();
			}
		});
		bt5.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				//执行代码
				exeCode();
			}
		});
		bar1.add(bt1);
		bar1.add(bt2);
		bar1.add(bt3);
		bar1.add(bt4);
		bar1.add(bt5);

		
		ImageIcon icon = new ImageIcon("imag\\file.png");
		JLabel openLable = new JLabel();
		SetIcon(openLable,icon,"打开");
		icon = new ImageIcon("imag\\save.png");
		JLabel saveLable = new JLabel();
		SetIcon(saveLable,icon,"保存");
		icon = new ImageIcon("imag\\copy.png");
		JLabel copyLable = new JLabel();
		copyLable.setEnabled(true);
		
		SetIcon(copyLable,icon,"复制");
		icon = new ImageIcon("imag\\paste.png");
		JLabel pasteLable = new JLabel();
		pasteLable.setEnabled(true);
		
		SetIcon(pasteLable,icon,"粘贴");
		icon = new ImageIcon("imag\\cut.png");
		JLabel cutLable = new JLabel();
		SetIcon(cutLable,icon,"剪切");
		
		p.add(bar2,FlowLayout.LEFT);
		p.add(bar1);
		return p;
	}

	public void SetIcon(JLabel label, ImageIcon icon, final String str) {
		Image image = icon.getImage();
		image = image.getScaledInstance(30, 30, Image.SCALE_DEFAULT);
		icon.setImage(image);
		label.setToolTipText(str);
		label.setIcon(icon);
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				if (str.endsWith("打开"))
					openFile();
				else if (str.endsWith("复制")) {
					copy();
				} else if (str.endsWith("保存")) {
					try {
						if(!sourseFile.getText().equals(""))//文本域非空才保存文件
							SaveFile(currentFile);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} else if (str.endsWith("粘贴")) {
					System.out.println("粘贴");
					past();
				} else if (str.endsWith("剪切")) {
					System.out.println("剪切");
					cut();
				}
			}
		});
		bar2.add(label);
		bar2.addSeparator();
	}
	/**
	 * 执行目标代码
	 */
	private void exeCode(){
		String code = "2205\nf000";
		String asmt = "load R3,1\nload R0,254[R3]\n"
				+"write R0\nhalt";
/*		String asm = "load R3,1\nload R0,254[R3]\nload R3,0\nadd R0,254[R3]\nload R3,0\n"
				+ "load R0,252[R3]\nload R0,@R0\nload R3,251\nload R3,253[R3]\nstore R0,@R3\n"
				+"write R0\nhalt";
				*/
		if(asm == null){
			return ;
		}
		//将汇编翻译为机器码
		Interpreter interpreter = null;
		try {
			code = readFile(asm.getAsmFile()).trim();
			interpreter = new Interpreter(code);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(interpreter.interpreterOK){
			code = interpreter.getMachIns().trim();
			MyDOS dos = new MyDOS();
			Exec exec = new Exec(code,dos);
			Thread thread = new Thread(exec);
			thread.start();
			dos.addWindowListener(new WindowListener() {
				
				@Override
				public void windowClosing(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void windowClosed(WindowEvent e) {
					// TODO Auto-generated method stub
					thread.stop();
				}

				@Override
				public void windowActivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowIconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowOpened(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
			
			});
		}
//		System.out.println((int)(Cpu.R0 & 0xffff) + "--R0");
	}
	/**
	 * 四元式转化成目标代码
	 */
	private void toASM(){
		if(semAnalysis == null || lexAnalysis == null){
			return ;
		}
		asm = new ASM(semAnalysis.fourElemList,lexAnalysis);
		try {
			consoleField.setText(readFile(asm.getAsmFile()).toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		signField.setText(lexAnalysis.getIdMapInfo());
	}
	/**
	 * 语义分析
	 */
	private void semAnalysis(){
		if(lexAnalysis!=null){
			semAnalysis = new SemAnalysis(lexAnalysis);
		}
		else{
			return ;
		}
		try {
			consoleField.setText(readFile(semAnalysis.outputFourElem()).toString());
//			System.out.println(readFile(grammarAnalysis.outputFourElem()).toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		errField.setText(semAnalysis.getError());
		signField.setText(lexAnalysis.getIdMapInfo());
	}
	/**
	 * 语法分析
	 */
	public void parse(){
		if(lexAnalysis!=null){
			grammarAnalysis = new GrammarAnalysis(lexAnalysis);
		}
		errField.setText(grammarAnalysis.getError());
		
		signField.setText(lexAnalysis.getIdMapInfo());
		
		tokenField.setText(grammarAnalysis.builder.toString()); 
		
	}

	/**
	 * 词法分析
	 */
	public void lexAnaly() {
		if (sourseFile.getText().equals("")) {
			consoleField.setText(null);// temp
			tokenField.setText(null);// temp
			signField.setText(null);// temp
			errField.setText(null);// temp
			return;
		}
//		tokenField.setText("0000");
		lexAnalysis = new LexAnalysis(sourseFile.getText());
		
		String string = "入口\t" + "token\t" + "类型\t" + "行\t" + "种属\t" + "值\t" + "内存地址\t\n";
		StringBuilder builder = new StringBuilder();
		builder.append(string);
		Iterator iter = lexAnalysis.wordMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			// Object key = entry.getKey();
			Words val = (Words) entry.getValue();
			string = val.getId() + "\t" + val.getWord() + "\t" + val.getType() + "\t" + val.getLine() + "\t"
					+ val.getAttr() + "\t" + val.getValue() + "\t" + val.getAddress() + "\t\n";
			builder.append(string);
		}
		tokenField.setText(builder.toString());
		
		
		signField.setText(lexAnalysis.getIdMapInfo());

		builder.delete(0, builder.length());
		string = "错误编号\t" + "错误信息\t\t" + "错误行\t" + "错误单词\t\n";
		builder.append(string);
		iter = lexAnalysis.errorMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			// Object key = entry.getKey();
			Error val = (Error) entry.getValue();
			string = val.getId() + "\t" + val.getInfo() + "\t" + val.getLine() + "\t" + val.getWord().getWord()
					+ "\t\n";
			builder.append(string);
		}
		builder.append("\t\t\t-----------------有"+lexAnalysis.getErrCount()+"处错误-----------------\n");
		errField.setText(builder.toString());
		
		consoleField.setText(builder.toString());
	}

	/*
	 * 文本框
	 */
	private Component createcCenterPane() {

		jSplitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);// 竖直分割
		jSplitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);// 水平分割
		jSplitPane2.setContinuousLayout(true);
		jSplitPane2.setOneTouchExpandable(true);

		JPanel p0 = new JPanel(new BorderLayout());
		// JLabel label0 = new JLabel(" workspace：");
		// p0.add( label0,BorderLayout.NORTH);

		JPanel p1 = new JPanel(new BorderLayout());
		JLabel label1 = new JLabel("  源文件：");
		sourseFile = new JTextPane();

		Font mf = new Font("宋体", Font.PLAIN, 30);
		sourseFile.setFont(mf);

		a = new String[500];
		for (int i = 0; i < 500; i++) {
			a[i] = String.valueOf(i + 1);
		}
		row = new JList(a);
		row.setForeground(Color.red);

		jScrollPane = new JScrollPane(sourseFile);
		mf = new Font("宋体", Font.PLAIN, 30);// 字体
		row.setFont(mf);
		jScrollPane.setRowHeaderView(row);
		sourseFile.setForeground(Color.BLACK);
		sourseFile.add(popupMenu);
		sourseFile.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				this_mousePressed(e);//弹出popuMenu
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
//				System.out.println("row numbers is :"+500);//获取表格的总行数
				//获取鼠标点击的行的位置（及行数）
				setTokenColor(getSurLine(e));
//				System.out.println(mousepoint.getX()+","+mousepoint.getY());
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}
		});
		p1.add(label1, BorderLayout.NORTH);
		p1.add(jScrollPane, BorderLayout.CENTER);

		JPanel p2 = new JPanel(new BorderLayout());
		JLabel label2 = new JLabel("  控制台：");
		consoleField = new JTextArea("");
		consoleField.setFont(new Font("宋体", Font.PLAIN, 20));
		consoleField.setEditable(false);
		consoleField.setForeground(Color.BLUE);
		consoleField.setEditable(false);
		JScrollPane consoleCroPanel = new JScrollPane(consoleField);// console
		consoleCroPanel.add(label2);
		p2.add(label2, BorderLayout.NORTH);
		p2.add(consoleCroPanel, BorderLayout.CENTER);

		jSplitPane1.add(creatTabbedPane(), JSplitPane.TOP);
		jSplitPane1.add(p2, JSplitPane.BOTTOM);// 控制台

		jSplitPane2.add(jSplitPane1, JSplitPane.RIGHT);
		jSplitPane2.add(p1, JSplitPane.LEFT);// 源文件

		jSplitPane2.setEnabled(true);
		jSplitPane2.setOneTouchExpandable(true);
		jSplitPane2.setDividerSize(4);

		return jSplitPane2;
	}
	/*
	 * 分析获得鼠标所在行
	 */
	private int getSurLine(MouseEvent e){
		int line =0;
		try {
			String tempStr = sourseFile.getText(0, sourseFile.getCaretPosition());
			String []arr = tempStr.split("\n");
			line = arr.length;
		} catch (BadLocationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		System.out.println("光标所在行"+line);
		return line;
	}

	/*
	 * 打开文件
	 */
	public void openFile() {

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));//当前路径设置为当前工程文件夹
		chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
			public boolean accept(File f) {
				currentFile = f;//当前打开文件
				String fname = f.getName().toLowerCase();
				return fname.endsWith(".txt") || fname.endsWith(".c") || f.isDirectory() || fname.endsWith(".cpp");
			}

			public String getDescription() {
				return "Text Files";
			}
		});
		int r = chooser.showOpenDialog(this);
		if (r == JFileChooser.APPROVE_OPTION) {
			currentFile = chooser.getSelectedFile();//当前打开文件
			String filename = chooser.getSelectedFile().getPath();
			System.out.println(filename);
			StringTokenizer token = new StringTokenizer(filename, "\\");
			ArrayList<DefaultMutableTreeNode> files = new ArrayList<DefaultMutableTreeNode>();
			int i = 1;
			files.add(root);
			while (token.hasMoreTokens()) {
				files.add(new DefaultMutableTreeNode(token.nextElement()));
				i++;
				model.insertNodeInto(files.get(i - 1), files.get(i - 2), 0);
			}
			// 读入文件
			String text;
			try {
				soursePath = chooser.getSelectedFile().getPath();// 获取源文件路径
				text = readFile(soursePath);
				sourseFile.setText(text);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static String readFile(String fileName) throws IOException {
		StringBuilder sbr = new StringBuilder();
		String str;
		FileInputStream fis = new FileInputStream(fileName);
		BufferedInputStream bis = new BufferedInputStream(fis);
		InputStreamReader isr = new InputStreamReader(bis, "gb2312");// 选择gb2312不影响读入汉字
		BufferedReader in = new BufferedReader(isr);
		while ((str = in.readLine()) != null) {
			sbr.append(str).append('\n');
		}
		in.close();
		return sbr.toString();
	}

	// 创建选项卡标签
	public JTabbedPane creatTabbedPane() {
		tabbedPane = new JTabbedPane(); // 创建选项卡面板对象
		// 创建标签
		tokenLabel = new JLabel("token串");
		signLabel = new JLabel("符号表");
		errLabel = new JLabel("错误信息");
		// 创建文本域
		tokenField = new JTextPane();
//		tokenField.setFont(new Font("宋体", Font.PLAIN, 20));
		tokenField.setEditable(false);

		signField = new JTextArea("");
		signField.setEditable(false);

		errField = new JTextArea("");
		errField.setEditable(false);
		// 创建面板
		tokenPanel = new JScrollPane(tokenField);
		signPanel = new JScrollPane(signField);
		errPanel = new JScrollPane(errField);
		

		// 将标签面板加入到选项卡面板对象上
		tabbedPane.addTab("token串", null, tokenPanel, "First panel");
		tabbedPane.addTab("符号表", null, signPanel, "Second panel");
		tabbedPane.addTab("错误信息", null, errPanel, "Third panel");

		return tabbedPane;
	}

	/*
	 * copy
	 */
	private void copy() {
		String tempText = sourseFile.getSelectedText();// 获取鼠标选中文件
//		System.out.println(tempText);
		// 创建能传输指定 String 的 Transferable。
		StringSelection editText = new StringSelection(tempText);
		/*
		 * 将剪贴板的当前内容设置到指定的 transferable 对象， 并将指定的剪贴板所有者作为新内容的所有者注册。
		 */
		clipboard.setContents(editText, null);
	}

	/*
	 * cut
	 */
	private void cut() {
		String tempText = sourseFile.getSelectedText();
		StringSelection editText = new StringSelection(tempText);
		clipboard.setContents(editText, null);
		int start = sourseFile.getSelectionStart();
		int end = sourseFile.getSelectionEnd();
		sourseFile.replaceSelection("");
	}

	/*
	 * past
	 */
	private void past() {
		Transferable contents = clipboard.getContents(this);
		DataFlavor flavor = DataFlavor.stringFlavor;
		if (contents.isDataFlavorSupported(flavor)) {
			try {
				String str;
				str = (String) contents.getTransferData(flavor);
				sourseFile.replaceSelection(str);
				/*
				Document docs = sourseFile.getDocument();//获得文本对象
				SimpleAttributeSet attrset = new SimpleAttributeSet();
				StyleConstants.setFontSize(attrset,sourseFile.getFont().getSize());
				try {
		            docs.insertString(docs.getLength(), "要插入的内容",attrset);//对文本进行追加
		        } catch (BadLocationException e) {
		            e.printStackTrace();
		        }*/
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			}
	}

	/*
	 * 设置token串区域颜色
	 */
	private void setTokenColor(int line){
		 tokenField.setCaretPosition(0);
		 String string = tokenField.getText();
		 String []arr = string.split("\n");
		 if(string.equals(""))
			 return;
		 tokenField.setText(string);
		 
		 DefaultStyledDocument doc=(DefaultStyledDocument)tokenField.getDocument();
		 SimpleAttributeSet a = new SimpleAttributeSet();
	     StyleConstants.setBackground(a,new Color(49, 151, 230));
	     StyleConstants.setForeground(a, Color.WHITE);
	     
	     int start = -1,end = arr.length-1;
	     if(lexAnalysis!=null){
	    	 Iterator iter = lexAnalysis.wordMap.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry entry = (Map.Entry) iter.next();
					int key = (int) entry.getKey();
					Words val = (Words) entry.getValue();
					if(val.getLine() == line){
						if(start == -1){
							start = key;
						}	
					}
					else if(start !=-1){
						if(val.getLine() != line){
//							System.out.println(key+"hello");
							end = key-1;
							break;
						}
					}
				}
	     }
	     if(start == -1){
	    	 //空白行
	    	 return;
	     }
	     if(end == arr.length-1)//越界
	    	 end = arr.length-2;
	     if(start == end && start == -1)
	    	 return;
	     
	     int startOff=0,endOff=0,i;
	     startOff = endOff = arr[0].length()+1;
	     
//	     System.out.println(start+"->"+end+"->"+arr.length);//起始和结束行计算正确
	     
    	 for(i = 1;i <= end+1 && i < arr.length;i++){
 	    	if(i <= start && start != 1){
 	    		startOff+=arr[i].length()+1;
 	    	}
 	    	endOff+=arr[i].length()+1;
 	     }
    	 if(end == arr.length - 1){
    		 endOff+=arr[arr.length - 1].length()+1;
    	 }
	     doc.setCharacterAttributes(startOff,endOff - startOff,a,false);//设置指定范围的文字样式
	     tokenField.setCaretPosition(startOff);
	}
	private void SaveFile(File file) throws Exception
	{
		if(file==null)
		{//如果文件名为空 则选择要保存文件到某个地方
			JFileChooser jfc=new JFileChooser();
			jfc.setCurrentDirectory(new File("."));//当前路径设置为当前工程文件夹
			jfc.showSaveDialog(jfc);
			file=jfc.getSelectedFile();
			if(file!=null)
			{
				try
				{
					file.createNewFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	    OutputStreamWriter output=null;
	    if(file!=null)
	    {
		    try 
		    {
				FileOutputStream fOutputStream=new FileOutputStream(file);
				output=new OutputStreamWriter(fOutputStream,"gb2312");
				output.write(sourseFile.getText());
	//			System.out.println("hel");
				output.flush();
				output.close();
			} 
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
	    }
	 }
	// main
	public static void main(String[] args) {
		CompileUi mainform = new CompileUi();// 新建一个窗体
		mainform.setVisible(true);
		mainform.jSplitPane2.setDividerLocation(0.4);// 分割比例设置一定要在setVisible(true)之后
		mainform.jSplitPane1.setDividerLocation(0.6);
	}
}
