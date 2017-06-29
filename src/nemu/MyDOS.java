package nemu;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;

import javax.annotation.processing.Messager;
import javax.swing.*;

/**
 * java面板模拟控制台
 * @author LiangTao
 *
 */
public class MyDOS extends JFrame {

	private static final long serialVersionUID = -5418344602348249043L;
	private JPanel pup = new JPanel();
	private JPanel pdown = new JPanel();
	private JTextField txtCommand = new JTextField(35);
	private JTextArea txtContent = new JTextArea();
	private String readBuffer = new String();
	private Boolean getEnter = false;
	
	private JButton btnExec = new JButton("Execute");

	public MyDOS() {
		
		// 指定框架的布局管理器
		setLayout(new BorderLayout());
		// 设置文本框,文本域字体
		txtCommand.setFont(new Font("", Font.BOLD, 18));
		txtContent.setFont(new Font("", Font.BOLD, 18));
		txtContent.setEditable(false);
		// 指定面板的布局
		pup.setLayout(new BorderLayout());
		pdown.setLayout(new BorderLayout());

		// 将文本域添加导面板中
		pup.add(txtContent);
		// 为文本域添加滚动条
		pup.add(new JScrollPane(txtContent));
		// 将文本框,按钮添加到面板中
		JLabel jLabel = new JLabel(" input：");
		pdown.add(jLabel,BorderLayout.WEST);
		pdown.add(txtCommand,BorderLayout.EAST);	
//		pdown.add(btnExec);

		// 添加面板到框架中
		this.add(pup, BorderLayout.CENTER);
		
		this.add(pdown, BorderLayout.SOUTH);

		Toolkit toolkit = Toolkit.getDefaultToolkit();// 获取本机信息
		Dimension screen = toolkit.getScreenSize();
		
		this.setTitle("MyDOS");
		this.setSize(666, 444);
		this.setResizable(false);
		this.setLocation(screen.width / 2 - this.getWidth() / 2, screen.height / 2 - this.getHeight() / 2);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		// 设置事件
		// 添加按钮事件
		btnExec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s;
				// 获取文本框中的命令
				String command = txtCommand.getText().trim();
				Process process;
				try {
					process = Runtime.getRuntime().exec("cmd /c " + command);
					// 截获被调用程序的DOS运行窗口的标准输出
					BufferedReader br = new BufferedReader(
							new InputStreamReader(process.getInputStream()));
					while ((s = br.readLine()) != null)
						txtContent.append(s + "\r\n");

					process.waitFor();
					txtCommand.setText("");
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		});

		// 添加键盘Enter事件
		txtCommand.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				// 当按下回车时
				if (e.getKeyCode() == KeyEvent.VK_ENTER ||
						e.getKeyCode() == KeyEvent.VK_TAB ||
						e.getKeyCode() == KeyEvent.VK_SPACE) {
					// 获取文本框中的命令
					String command = txtCommand.getText().trim();
					System.out.println("已获取输入"+command);
					readBuffer += command;
					getEnter = true;
				}
			}


			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		});
	}
	
	/**
	 * 将write拼接到txtContext
	 * @param write
	 */
	public void refreshDos(String write){
		String tempStr = txtContent.getText();
		tempStr += write;
		txtContent.setText(tempStr);
		//将光标定位到文本末尾
		txtContent.setCaretPosition(txtContent.getText().length());
	}
	/**
	 * 读取readBuffer并清空输入缓冲区
	 * @return
	 */
	public String getReadBuffer(){
		refreshDos(">");
		//设置指针到文本末尾
		getEnter = false;
		while(!getEnter){
			try {			//暂停等待输入直到回车
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getEnter = false;
		String str = readBuffer;
		readBuffer = "";
		txtCommand.setText(readBuffer);
		return str;
	}
	
	public static void main(String[] args) {
		MyDOS frame = new MyDOS();
	}
}
