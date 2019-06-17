package org.emoflon.neo.emsl.ui.editor;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.emoflon.neo.emsl.ui.visualisation.PlatformFragmentLinkOpener;

public class Java2EMSLHyperlinkDetector extends AbstractHyperlinkDetector {

	public Java2EMSLHyperlinkDetector() {

	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}

		IDocument document = textViewer.getDocument();

		if (document == null) {
			return null;
		}

		int offset = region.getOffset();
		IRegion lineInfo;
		String line;

		try {
			lineInfo = document.getLineInformationOfOffset(offset);
			lineInfo = document.getLineInformationOfOffset(lineInfo.getOffset() - 1);
			line = document.get(lineInfo.getOffset(), lineInfo.getLength()).trim();
		} catch (BadLocationException e) {
			return null;
		}

		if (line.startsWith("//:~>")) {
			var uri = line.replace("//:~>", "").trim();
			return new IHyperlink[] { new IHyperlink() {
				@Override
				public void open() {
					var opener = new PlatformFragmentLinkOpener();
					opener.openLink(URI.createURI(uri));
				}

				@Override
				public String getTypeLabel() {
					return "Open EMSL Entity";
				}

				@Override
				public String getHyperlinkText() {
					return getTypeLabel();
				}

				@Override
				public IRegion getHyperlinkRegion() {
					return region;
				}
			} };
		}

		return null;
	}

}
