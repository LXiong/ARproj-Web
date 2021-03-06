package com.mango.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
  
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
  
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.mango.entityManager.UserManager;
import com.mango.utils.JsonEncodeFormatter;

/**
 * Servlet implementation class IconServlet
 */
public class IconServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	 // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "/WEB-INF/icons";
  
    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public IconServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("http://120.78.177.77/error.html");
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
				
		String code = request.getParameter("code").trim();
		String token = request.getParameter("token").trim();
		
		UserManager userManager = new UserManager();
		if(userManager.checkToken(token)) {
			if("10022".compareTo(code)==0) {
				this.updateUserIcon(request,response);
				return;
			}
			if("10023".compareTo(code)==0) {
				this.getUserIcons(request,response);
				return;
			}
			else {
				response.getWriter().write(JsonEncodeFormatter.universalResponse(90001, "Invalid Request Code."));
				return;	
			}
		}
		else {
			response.getWriter().write(JsonEncodeFormatter.universalResponse(90009, "Token Invalid or expired."));
			return;	
		}
	}
	
	
	
	private void getUserIcons(HttpServletRequest request,HttpServletResponse response) throws IOException {
		
        response.setContentType("Content-Type: application/octet-stream;charset=utf-8"); //设置返回的文件类型   

		String uuid = request.getParameter("uuid");
		FileInputStream fis;
		String imagePath = getServletContext().getRealPath("/WEB-INF/icons/"+uuid+".jpg");    
		try {
			fis = new FileInputStream(imagePath);
			int size =fis.available(); //得到文件大小   
	        byte data[]=new byte[size];   
	        fis.read(data);  //读数据   
	        fis.close();   
	        OutputStream os = response.getOutputStream();  
	        os.write(data);  
	        os.flush(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			response.getWriter().write(JsonEncodeFormatter.universalResponse(90006, "Illegal request header parameters."));
			return;
		}  
               
	}

	private void updateUserIcon(HttpServletRequest request,HttpServletResponse response) throws IOException {
		response.setHeader("Content-Type", "application/json;charset=utf8");  
		
		String uuid = request.getParameter("uuid").trim();
		
		// 检测是否为多媒体上传
        if (!ServletFileUpload.isMultipartContent(request)) {
            // 如果不是则停止
            response.getWriter().write(JsonEncodeFormatter.universalResponse(90006, "Illegal request header parameters."));
            return;
        }
  
        // 配置上传参数
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        // 设置临时存储目录
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
  
        ServletFileUpload upload = new ServletFileUpload(factory);
          
        // 设置最大文件上传值
        upload.setFileSizeMax(MAX_FILE_SIZE);
          
        // 设置最大请求值 (包含文件和表单数据)
        upload.setSizeMax(MAX_REQUEST_SIZE);
  
        // 构造临时路径来存储上传的文件
        // 这个路径相对当前应用的目录
        String uploadPath = getServletContext().getRealPath("./") + File.separator + UPLOAD_DIRECTORY;
        
          
        // 如果目录不存在则创建
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
  
        try {
            // 解析请求的内容提取文件数据
            List<FileItem> formItems = upload.parseRequest(request);
  
            if (formItems != null && formItems.size() > 0) {
                // 迭代表单数据
                for (FileItem item : formItems) {
                    // 处理不在表单中的字段
                    if (!item.isFormField()) {
                    System.out.println(item.getName());
                    	String fileName = new File(uuid+".jpg").getName();
                    	String filePath = uploadPath + File.separator + fileName;
                        File storeFile = new File(filePath);
                        // 在控制台输出文件的上传路径
                        System.out.println(filePath);
                        // 保存文件到硬盘
                        item.write(storeFile);
                        request.setAttribute("message",
                            "文件上传成功!");
                    }
                }
                
                response.getWriter().write(JsonEncodeFormatter.universalResponse(0, "ok"));
                return;
            }
        } catch (Exception ex) {
        		ex.printStackTrace();
            request.setAttribute("message",
                    "错误信息: " + ex.getMessage());
            response.getWriter().write(JsonEncodeFormatter.universalResponse(90017, "File uploaded failed."));
            return;
        }
	}

}
