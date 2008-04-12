package org.punksearch.web.chart;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jfree.chart.JFreeChart;

import com.keypoint.PngEncoder;

public class ChartViewer extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String uri = request.getRequestURI();
		String attribute = uri.substring(uri.lastIndexOf("/") + 1);
		
		HttpSession session = request.getSession(true);
		// get the chart from storage
		JFreeChart chart = (JFreeChart) session.getAttribute(attribute);
		// set the content type so the browser can see this as it is
		response.setContentType("image/png");

		// send the picture
		BufferedImage buf = chart.createBufferedImage(640, 400, null);
		PngEncoder encoder = new PngEncoder(buf, false, 0, 9);
		response.getOutputStream().write(encoder.pngEncode());
	}

}
