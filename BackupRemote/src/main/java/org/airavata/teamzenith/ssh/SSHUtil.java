package org.airavata.teamzenith.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.airavata.teamzenith.config.SSHPropertyHandler;
import org.airavata.teamzenith.utils.FilePathFinder;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.compress.utils.IOUtils;

/**
 * @author Anuj
 * All the SSH related functions are encapsulated in this class
 */
public class SSHUtil {
	
	public Session createSession(){
		try{
			JSch jsch=new JSch();
			FilePathFinder fpf=new FilePathFinder();
			SSHPropertyHandler sph=new SSHPropertyHandler();
			Properties p=sph.getPropertyMap();
			String user = p.getProperty("user");
			String host = p.getProperty("host");
	        int port =    Integer.parseInt(p.getProperty("port"));
	        String privateKeyPath = p.getProperty("privatekeypath");
	        String privateKeyFile= privateKeyPath+p.getProperty("privatekeyfile");
	        
	        String prkey=p.getProperty("ppk");
	        String pubkey=p.getProperty("pub");
	        byte [] prBytes=prkey.getBytes();
	        byte [] pubBytes=pubkey.getBytes();
	        byte [] passwd="".getBytes();
	     
	        File f=new File(getClass().getResource("/puttyKey.ppk").getFile());
	     
		  // jsch.addIdentity(fpf.splitPath(f.getAbsolutePath()));
	        //jsch.addIdentity("Remote", prBytes, pubBytes, passwd);
	        byte[] privateKey =  IOUtils.toByteArray(getClass().getResourceAsStream("/puttyKey.ppk"));
		    byte[] publicKey =  IOUtils.toByteArray(getClass().getResourceAsStream("/id_rsa.pub"));
	   
		    jsch.addIdentity("sshAuthAtul", privateKey, publicKey, (byte[])null);
       
	        Session session = jsch.getSession(user, host, port);
	        //System.out.println("session created.");
	        session.setConfig("StrictHostKeyChecking", "no");
	        return session;
		}
		catch(Exception e){
		      System.out.println(e);
		      e.printStackTrace();
		      return null;
		    }
	}
	
	public void sessionStart(Session session){
		try {
			session.connect();
			System.out.println("Session connected successfully !!!");
		} catch (Exception e) {
			// TODO: handle exception
			 System.err.println(e);
		}
	}
	
	
	public void sessionStop(Session session){
		try {
			session.disconnect();
			System.out.println("Session disconnected !!!");
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println(e);
		}
	}
	
	
	public boolean ScpTo(Session session, String source, String dest) throws JSchException, IOException{
		


		InputStream fis=null;
		boolean ptimestamp = false;
		try{
			// exec 'scp -t rfile' remotely
			String command="scp " + (ptimestamp ? "-p" :"") +" -t "+dest;
			Channel channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out=channel.getOutputStream();
			InputStream in=channel.getInputStream();

			channel.connect();
			
			
			if(checkAck(in)!=0){
				System.out.println("first");
				System.exit(0);
			}
            
			//ClassLoader classLoader = getClass().getClassLoader();
			//File _lfile = new File(classLoader.getResource("/"+source).getFile());
			//File _lfile  =new File(getClass().getResource("/a.c").getFile());
			//File _lfile = new File(source);

			/*if(ptimestamp){
				command="T"+(_lfile.lastModified()/1000)+" 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command+=(" "+(_lfile.lastModified()/1000)+" 0\n"); 
				System.out.println("Command is "+command);
				out.write(command.getBytes()); out.flush();
				if(checkAck(in)!=0){
					System.exit(0);
				}
			}*/
			byte[] buf =  IOUtils.toByteArray(getClass().getResourceAsStream("/"+source));
			int size = buf.length;
			// send "C0644 filesize filename", where filename should not include '/'
			int filesize = size;
			command="C0644 "+filesize+" "+source;
			/*if(source.lastIndexOf('/')>0){
				command+=source.substring(source.lastIndexOf('/')+1);
			}
			else{
				command+=source;
			}*/
			command+="\n";
			out.write(command.getBytes()); out.flush();
			if(checkAck(in)!=0){
				System.out.println("second");
				System.exit(0);
			}
            //System.out.println(command);
           // fis=new FileInputStream(_lfile);
			// send a content of lfile
			//fis= getClass().getResourceAsStream("/"+source);
            
//			byte[] buf=new byte[1024];
//			/*while(true){
//				int len=fis.read(buf, 0, buf.length);
//				if(len<=0) break;
//				out.write(buf, 0, len); out.flush();
//			}
//			*/
//			int i;
//			while((i=fis.read())!=-1){
//				out.write(i);
//			}
//			fis.close();
//			fis=null;
//			// send '\0'
//			buf[0]=0; out.write(buf, 0, 1); out.flush();
//			buf[0] out= 0;
//			int i;
//			while((i=fis.read())!=-1){
//				out.write(i);
//			}
			
			
//			for(int i=0; i<size; i++){
//				out.write(buf, i, i+1); out.flush();
//			}
			out.write(buf); out.flush();
			buf[0]=0; out.write(buf,0,1);out.flush();
		
			
			/*if(checkAck(in)!=0){
				System.out.println(buf);
				System.out.println("third");
				System.exit(0);
			}*/
			out.close();

			channel.disconnect();
			
			System.out.println("Scp successful !!!");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
			if(fis!=null){
				fis.close();
			}
			}catch(Exception ee){}
            
		}
		return true;

	}
	static int checkAck(InputStream in) throws IOException{
	    int b=in.read();
	    // b may be 0 for success,
	    //          1 for error,
	    //          2 for fatal error,
	    //          -1
	    if(b==0) return b;
	    if(b==-1) return b;

	    if(b==1 || b==2){
	      StringBuffer sb=new StringBuffer();
	      int c;
	      do {
		c=in.read();
		sb.append((char)c);
	      }
	      while(c!='\n');
	      if(b==1){ // error
		System.out.print(sb.toString());
	      }
	      if(b==2){ // fatal error
		System.out.print(sb.toString());
	      }
	    }
	    return b;
	  }
	public boolean executeCommand(Session session, String command) throws JSchException, IOException{
		
		try{

			Channel channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);

			// X Forwarding
			// channel.setXForwarding(true);

			//channel.setInputStream(System.in);
			channel.setInputStream(null);

			//channel.setOutputStream(System.out);

			//FileOutputStream fos=new FileOutputStream("/tmp/stderr");
			//((ChannelExec)channel).setErrStream(fos);
			((ChannelExec)channel).setErrStream(System.err);

			InputStream in=channel.getInputStream();

			channel.connect();

			byte[] tmp=new byte[1024];
			while(true){
				while(in.available()>0){
					int i=in.read(tmp, 0, 1024);
					if(i<0)break;
					System.out.print(new String(tmp, 0, i));
				}
				if(channel.isClosed()){
					if(in.available()>0) continue;
					System.out.println("exit-status: "+channel.getExitStatus());
					break;
				}
				try{Thread.sleep(1000);}catch(Exception ee){}
			}
			channel.disconnect();
			System.out.println(command +": Executed successfully !!!");
			return true;
		}
		finally{
			
		}
	}

}