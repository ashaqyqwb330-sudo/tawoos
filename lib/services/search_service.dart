import '../data/programs_faq.dart';
import '../data/programs_data.dart';

class SearchService {
  /// Localized intelligence engine that mimics real dynamic queries
  static String askProgram(String programId, String query) {
    final String cleanQuery = _normalizeArabic(query);
    if (cleanQuery.isEmpty) {
      return "يرجى كتابة استفسارك الطبي أو التكتيكي بوضوح.";
    }

    // Attempt to match with existing faq
    final List<Map<String, String>> faqs = ProgramsFaq.getFaqByProgramId(programId);
    Map<String, String>? bestMatch;
    double highestScore = 0.0;

    for (var faqItem in faqs) {
      final question = faqItem['question'] ?? '';
      final normalizedQ = _normalizeArabic(question);

      final qWords = normalizedQ.split(' ').where((w) => w.length > 2).toSet();
      final queryWords = cleanQuery.split(' ').where((w) => w.length > 2).toSet();

      if (qWords.isEmpty || queryWords.isEmpty) continue;

      final intersection = qWords.intersection(queryWords);
      final score = intersection.length / (qWords.length + queryWords.length - intersection.length);

      if (score > highestScore) {
        highestScore = score;
        bestMatch = faqItem;
      }
    }

    if (highestScore > 0.18 && bestMatch != null) {
      return bestMatch['answer'] ?? '';
    }

    // Try finding in master list info
    final ProgramInfo? program = ProgramsData.getProgramById(programId);
    if (program != null) {
      if (cleanQuery.contains('شروط') || cleanQuery.contains('قبول')) {
        return program.admissionRequirements;
      }
      if (cleanQuery.contains('تخرج') || cleanQuery.contains('متطلبات')) {
        return program.graduationRequirements;
      }
    }

    return "لم أجد إجابة تكتيكية مطابقة في الوثائق المعتمدة لهذا الاستفسار. هل تود الاستفسار عن الشروط العامة أو الخطة الدراسية الخاصة بهذا القسم؟";
  }

  static String _normalizeArabic(String text) {
    String clean = text.toLowerCase();
    clean = clean.replaceAll(RegExp(r'[أإآ]'), 'ا');
    clean = clean.replaceAll(RegExp(r'[ى]'), 'ي');
    clean = clean.replaceAll(RegExp(r'[ة]'), 'ه');
    clean = clean.replaceAll(RegExp(r'[ًٌٍَُِّْ]'), ''); // Remove diacritics
    clean = clean.replaceAll('؟', '').replaceAll('?', '').replaceAll('.', '');
    return clean.trim();
  }
}
