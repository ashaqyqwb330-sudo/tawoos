class AiEngineResult {
  final String answer; // The current step instruction or full matched text
  final List<String> steps; // All parsed steps
  final int currentStepIndex; // 0-based index of the current step
  final List<Map<String, String>> choices; // Similar alternative FAQ items to present
  final bool hasMultipleChoices;
  final bool isSequential;

  AiEngineResult({
    required this.answer,
    required this.steps,
    required this.currentStepIndex,
    required this.choices,
    required this.hasMultipleChoices,
    required this.isSequential,
  });
}

class LocalAiEngine {
  static String normalizeArabic(String text) {
    String clean = text.toLowerCase();
    clean = clean.replaceAll(RegExp(r'[أإآ]'), 'ا');
    clean = clean.replaceAll(RegExp(r'[ى]'), 'ي');
    clean = clean.replaceAll(RegExp(r'[ة]'), 'ه');
    clean = clean.replaceAll(RegExp(r'[ًٌٍَُِّْ]'), ''); // Remove diacritics
    clean = clean.replaceAll('؟', '').replaceAll('?', '').replaceAll('.', '');
    return clean.trim();
  }

  static double calculateSimilarity(String q1, String q2) {
    final norm1 = normalizeArabic(q1);
    final norm2 = normalizeArabic(q2);

    final words1 = norm1.split(' ').where((w) => w.length > 2).toSet();
    final words2 = norm2.split(' ').where((w) => w.length > 2).toSet();

    if (words1.isEmpty || words2.isEmpty) return 0.0;

    final intersection = words1.intersection(words2);
    // Jaccard similarity index
    final score = intersection.length / (words1.length + words2.length - intersection.length);
    return score;
  }

  // Parse any long text into sequential steps
  static List<String> parseSteps(String text) {
    // Replace inline digit patterns format "1. " or "2-" with newlines to split them easily
    String prepared = text;
    prepared = prepared.replaceAllMapped(RegExp(r'\s+(\d+[\.\-\)])'), (match) {
      return '\n' + (match.group(1) ?? '');
    });

    // Handle sequential symbols or headings in Arabic
    prepared = prepared.replaceAll('أولاً:', '\nأولياً:')
                       .replaceAll('ثانياً:', '\nثانياً:')
                       .replaceAll('ثالثاً:', '\nثالثاً:')
                       .replaceAll('رابعاً:', '\nرابعاً:')
                       .replaceAll('خامساً:', '\nخامساً:');

    final lines = prepared.split('\n').map((e) => e.trim()).where((e) => e.isNotEmpty).toList();
    final List<String> parsedSteps = [];

    String currentMerged = "";
    for (var line in lines) {
      bool isNewStep = RegExp(r'^(\d+[\.\-\)]|\*|•|📍|🔹|♦|أولاً|ثانياً|ثالثاً|رابعاً|خامساً)').hasMatch(line);
      if (isNewStep) {
        if (currentMerged.isNotEmpty) {
          parsedSteps.add(currentMerged);
        }
        currentMerged = line;
      } else {
        if (currentMerged.isEmpty) {
          currentMerged = line;
        } else {
          currentMerged += "\n" + line;
        }
      }
    }
    if (currentMerged.isNotEmpty) {
      parsedSteps.add(currentMerged);
    }

    if (parsedSteps.isEmpty) {
      parsedSteps.add(text);
    }
    return parsedSteps;
  }

  // Process user message and returns detailed AI result
  static AiEngineResult query(String userQuery, List<Map<String, String>> faqList, {int? forceStepIndex}) {
    final queryText = userQuery.trim();
    if (queryText.isEmpty) {
      return AiEngineResult(
        answer: 'تفضّل بطرح استفسارك الأكاديمي أو الميداني وسأجيبك بكل دقّة.',
        steps: [],
        currentStepIndex: 0,
        choices: [],
        hasMultipleChoices: false,
        isSequential: false,
      );
    }

    // 1. Calculate similarity for each FAQ
    final List<Map<String, dynamic>> scoredFaqs = [];
    for (var faq in faqList) {
      final question = faq['question'] ?? '';
      final score = calculateSimilarity(queryText, question);
      scoredFaqs.add({
        'faq': faq,
        'score': score,
      });
    }

    // Sort by score descending
    scoredFaqs.sort((a, b) => (b['score'] as double).compareTo(a['score'] as double));

    // Determine candidates / multiple choices within a reasonable confidence range
    final double highestScore = scoredFaqs.isNotEmpty ? (scoredFaqs.first['score'] as double) : 0.0;
    
    final List<Map<String, String>> choices = [];
    if (highestScore > 0.08) {
      for (var sFaq in scoredFaqs) {
        final score = sFaq['score'] as double;
        // If score is also high but not the absolute winner
        if (score > 0.08 && score < highestScore && (highestScore - score) < 0.20) {
          choices.add(Map<String, String>.from(sFaq['faq'] as Map));
        }
        if (choices.length >= 3) break;
      }
    }

    // Pick top matched answer
    Map<String, String>? bestMatch;
    if (highestScore > 0.12) {
      bestMatch = Map<String, String>.from(scoredFaqs.first['faq'] as Map);
    }

    if (bestMatch == null) {
      // Find similar options (from non-zero score items) if any to offer user
      final fallbackChoices = scoredFaqs
          .where((e) => (e['score'] as double) > 0.04)
          .map((e) => Map<String, String>.from(e['faq'] as Map))
          .take(4)
          .toList();

      return AiEngineResult(
        answer: '', 
        steps: [],
        currentStepIndex: 0,
        choices: fallbackChoices,
        hasMultipleChoices: fallbackChoices.isNotEmpty,
        isSequential: false,
      );
    }

    final fullAnswer = bestMatch['answer'] ?? '';
    final steps = parseSteps(fullAnswer);
    final isSeq = steps.length > 1;

    int activeIndex = forceStepIndex ?? 0;
    if (activeIndex >= steps.length) {
      activeIndex = steps.length - 1;
    }

    String displayAnswer = fullAnswer;
    if (isSeq) {
      displayAnswer = steps[activeIndex];
    }

    return AiEngineResult(
      answer: displayAnswer,
      steps: steps,
      currentStepIndex: activeIndex,
      choices: choices,
      hasMultipleChoices: choices.isNotEmpty,
      isSequential: isSeq,
    );
  }
}
