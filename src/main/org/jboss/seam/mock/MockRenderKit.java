package org.jboss.seam.mock;

import java.io.OutputStream;
import java.io.Writer;

import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.Renderer;
import javax.faces.render.ResponseStateManager;

public class MockRenderKit extends RenderKit {

   @Override
   public void addRenderer(String arg0, String arg1, Renderer arg2) {
      // TODO Auto-generated method stub

   }

   @Override
   public Renderer getRenderer(String arg0, String arg1) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ResponseStateManager getResponseStateManager() {
      return new MockResponseStateManager();
   }

   @Override
   public ResponseWriter createResponseWriter(Writer arg0, String arg1,
         String arg2) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public ResponseStream createResponseStream(OutputStream arg0) {
      // TODO Auto-generated method stub
      return null;
   }

}
