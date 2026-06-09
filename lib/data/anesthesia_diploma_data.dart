import 'programs_data.dart';

class AnesthesiaDiplomaData {
  static const String id = 'anesthesia_diploma';
  
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
