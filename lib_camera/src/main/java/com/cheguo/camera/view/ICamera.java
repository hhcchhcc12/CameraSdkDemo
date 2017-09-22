package com.cheguo.camera.view;

import java.io.IOException;

public interface ICamera {

	/**
	 *
	 * @param data //图片字节
	 * @param degree //翻转角度
	 * @param cameraNum //0代表后置摄像头,1代表前置摄像头
	 * @throws IOException
	 */
	public void saveToSDCard(byte[] data,int degree,int cameraNum) throws IOException ;

	/**
	 *
	 * @param max zoombar最大指
	 */
	public void setSeekBar(int min,int max,int cur);

	public void cameraError(Exception e);

}
