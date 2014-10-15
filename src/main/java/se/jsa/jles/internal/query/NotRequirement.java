package se.jsa.jles.internal.query;

import se.jsa.jles.internal.Constraint;
import se.jsa.jles.internal.FieldConstraint;

class NotRequirement extends Requirement {

	private Requirement requirement;

	public NotRequirement(Requirement requirement) {
		this.requirement = requirement;
	}

	@Override
	public FieldConstraint createFieldContraint() {
		FieldConstraint fieldContraint = requirement.createFieldContraint();
		return FieldConstraint.create(fieldContraint.getFieldName(), new NotConstraint(fieldContraint.getConstraint()));
	}
	
	private static class NotConstraint extends Constraint {
		private Constraint constraint;

		public NotConstraint(Constraint constraint) {
			this.constraint = constraint;
		}
		
		@Override
		protected boolean isSatisfied(Object eventFieldValue) {
			return !constraint.isSatisfiedBy(eventFieldValue);
		}

		@Override
		public Class<?> getFieldType() {
			return constraint.getFieldType();
		}
	}

}
