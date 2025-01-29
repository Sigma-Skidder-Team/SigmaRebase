package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.FullBox;

//needs to be defined, because readChildren() is not called by factory
/* TODO: this class shouldn't be needed. at least here, things become too
complicated. change this!!! */
public class MetaBox extends FullBox {

	public MetaBox() {
		super("Meta Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		// some encoders (such as Android's MexiaMuxer) do not include
		// the version and flags fields in the meta box, instead going
		// directly to the hdlr box
		long possibleType = in.peekBytes(8) & 0xFFFFFFFFL;
		if(possibleType != BoxTypes.HANDLER_BOX){
			super.decode(in);
		}
		readChildren(in);
	}
}
