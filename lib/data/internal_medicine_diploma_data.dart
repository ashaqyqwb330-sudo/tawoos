import 'programs_data.dart';

class InternalMedicineDiplomaData {
  static const String id = 'internal_medicine_diploma';
  
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
