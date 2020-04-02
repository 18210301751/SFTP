# SFTP
定时上传文件到服务器上。

1.建议定时器用spring提供的。
2.我用的sftp做的文件上传，二次封装了。
3.导入依赖jar包
<!-- Jsch -->
		<dependency>
			<groupId>com.jcraft</groupId>
			<artifactId>jsch</artifactId>
			<version>0.1.55</version>
		</dependency>
	</dependencies>

4.put();方法是上传方法，有多个重载方法，可根据需要选择想要返回的对象类型。此项目我用的是流对象接收。
5.get();方法是下载方法，也有多个重载方法。
6.除此以外像ls();、cd();mkdir();等方法可以辅助方法目录。
