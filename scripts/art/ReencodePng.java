import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Re-saves PNG files with Java's standard ImageIO encoder (same pixels).
 * Run with: java scripts/art/ReencodePng.java <file or folder>...
 * generate_skins.py / generate_items.py call this after painting, so every shipped PNG
 * comes out of a mainstream encoder.
 */
public class ReencodePng {
	public static void main(String[] args) throws IOException {
		for (String arg : args) {
			Path root = Path.of(arg);
			List<Path> files;
			try (var stream = Files.walk(root)) {
				files = stream.filter(p -> p.toString().endsWith(".png")).toList();
			}
			for (Path file : files) {
				BufferedImage src = ImageIO.read(file.toFile());
				BufferedImage argb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
				argb.getGraphics().drawImage(src, 0, 0, null);
				File tmp = new File(file + ".tmp");
				ImageIO.write(argb, "png", tmp);
				Files.move(tmp.toPath(), file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				System.out.println("re-encoded " + file);
			}
		}
	}
}
