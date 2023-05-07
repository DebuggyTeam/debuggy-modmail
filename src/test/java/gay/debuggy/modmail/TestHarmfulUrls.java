package gay.debuggy.modmail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestHarmfulUrls {
	@Test
	public void testBatch() {
		Assertions.assertTrue(ModmailCommon.isHarmful("https://example.com/i_am_innocent.bat"));
	}
}
