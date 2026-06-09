import 'programs_data.dart';

class CombatMedicDiplomaData {
  static const String id = 'combat_medic_diploma';
  
  static ProgramInfo getProgram() {
    return ProgramsData.getAllPrograms().firstWhere((element) => element.id == id);
  }

  static String getAdmission() {
    return getProgram().admissionRequirements;
  }

  static String getGraduationRequirements() {
    return getProgram().graduationRequirements;
  }
}
