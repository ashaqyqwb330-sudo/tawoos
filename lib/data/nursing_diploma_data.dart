import 'programs_data.dart';

class NursingDiplomaData {
  static const String id = 'nursing_diploma';
  
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
