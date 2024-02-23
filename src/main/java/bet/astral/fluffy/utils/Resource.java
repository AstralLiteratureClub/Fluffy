package bet.astral.fluffy.utils;

import java.io.*;
import java.util.Objects;
import java.util.stream.Stream;

public final class Resource {
	private static File loadResourceAsFile(String fileName) {
		InputStream inputStream = Resource.class.getResourceAsStream(fileName);
		if (inputStream == null) {
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Resource.class.getResourceAsStream(fileName))));
		String[] split = fileName.split("\\.");
		try {
			File file = File.createTempFile(fileName, split[split.length-1]);
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
			reader.lines().forEachOrdered(line->{
				try {
					fileWriter.write(line);
					fileWriter.newLine();
					fileWriter.flush();
				} catch (IOException e) {
					try {
						fileWriter.close();
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					}
					throw new RuntimeException(e);
				}
			});
			fileWriter.close();
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				inputStream.close();
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static File loadResourceAsTemp(String fileName, String end) {
		try {
			try {
				File file = loadResourceAsFile(fileName+"." + end);
				File temp = File.createTempFile(fileName, end);
				BufferedReader fileReader = new BufferedReader(new FileReader(file));
				BufferedWriter fileWriter = new BufferedWriter(new FileWriter(temp));
				Stream<String> lines = fileReader.lines();
				lines.forEachOrdered(line -> {
					try {
						fileWriter.write(line);
						fileWriter.newLine();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
				fileWriter.flush();

				fileReader.close();
				fileWriter.close();

				return temp;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} catch (NullPointerException e){
			throw new RuntimeException("Could not find file for id: "+ fileName+"."+end);
		}
	}

	public static File loadResourceToFile(String fileName, String end, File file, boolean exists){
		if (file.exists() && exists){
			return file;
		} else if (!file.exists()){
			try {
				if (!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Couldn't create new file for: "+ file.getName(), e);
			}
		}
		try {
			File fileTemp = loadResourceAsFile(fileName+"."+end);
			if (fileTemp == null){
				throw new RuntimeException("Could not find file for id: "+ file);
			}
			BufferedReader  fileReader = new BufferedReader(new FileReader(fileTemp));
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
			Stream<String> lines = fileReader.lines();
			lines.forEachOrdered(line->{
				try {
					fileWriter.write(line);
					fileWriter.newLine();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			fileWriter.flush();

			fileReader.close();
			fileWriter.close();

			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}