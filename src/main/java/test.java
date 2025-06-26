public class test {

    public static void main(String[] args) {
        String word2 = "오봉집 보쌈";
        String word1 = "오봉집 보123쌈(소)";
        
        double similarityPercentage = getSimilarityPercentage(word1, word2);
        System.out.println("두 단어의 유사도: " + similarityPercentage + "%");
    }

    public static double getSimilarityPercentage(String str1, String str2) {
        int maxLen = Math.max(str1.length(), str2.length());

        if (maxLen == 0) {
            return 100.0; // 두 문자열이 모두 비어있다면 100% 유사함.
        }

        int distance = levenshteinDistance(str1, str2);
        return (1.0 - (double) distance / maxLen) * 100.0; // 유사도 백분율 계산
    }

    public static int levenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            for (int j = 0; j <= str2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j; // str1이 비어있을 때
                } else if (j == 0) {
                    dp[i][j] = i; // str2가 비어있을 때
                } else if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // 문자 일치
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1,    // 삭제
                                                   dp[i][j - 1] + 1),   // 삽입
                                                   dp[i - 1][j - 1] + 1); // 대체
                }
            }
        }
        return dp[str1.length()][str2.length()];
    }
}