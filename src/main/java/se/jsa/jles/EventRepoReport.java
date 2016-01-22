package se.jsa.jles;

import java.util.ArrayList;
import java.util.List;

public class EventRepoReport {

	private final List<String> lines = new ArrayList<String>();

	public EventRepoReport appendLine(String description) {
		lines.add(description);
		return this;
	}

	public EventRepoReport appendReport(String reportName, EventRepoReport status) {
		lines.add(reportName + ":");
		for (String line : status.lines) {
			this.lines.add("  " + line);
		}
		return this;
	}

	public List<String> getLines() {
		return lines;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (String line : lines) {
			stringBuilder.append(line).append('\n');
		}
		return stringBuilder.toString();
	}

}
