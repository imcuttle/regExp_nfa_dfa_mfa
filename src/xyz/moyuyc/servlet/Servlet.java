package xyz.moyuyc.servlet;

import net.sf.json.JSONObject;
import xyz.moyuyc.graph.ALGraph;
import xyz.moyuyc.model.BinTree;
import xyz.moyuyc.model.RegExpress;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Yc on 2016/3/22 for compiler2.
 */
@WebServlet(name = "Servlet",urlPatterns = "/get.do")
public class Servlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        super.service(req, res);
        String act = req.getParameter("act");
        String value = req.getParameter("value");
        if(act.equals("inputRE")) {
            RegExpress re = new RegExpress(value);//(a|b)*a(a|b)  a*|s*d  (a|b)a(a|b|c)*
            BinTree root = null;
            try {
                root = re.makeTree();
            } catch (Exception e) {
                e.printStackTrace();
                res.getWriter().print("error");
                return;
            }
            root.print();
            ALGraph g = root.makeNFAGraph("NFA of \""+value+"\"");
            ALGraph dfa = g.toDFAGraph();
            dfa.setTitle("DFA of \""+value+"\"");
            ALGraph mfa = dfa.toMFAGraph();
            mfa.setTitle("MFA of \""+value+"\"");
            JSONObject o = new JSONObject();
            o.put("nfa",g.toString());
            o.put("dfa",dfa.toString());
            o.put("mfa",mfa.toString());
            res.getWriter().print(o);
        }
    }
}
