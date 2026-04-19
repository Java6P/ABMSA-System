"""
ABMSA Model Service
Aspect-Based Multimodal Sentiment Analysis - Flask API Service

This is a mock/stub implementation for development and demonstration.
In production, replace with actual ABSA model (e.g., BERT-based, VLP-based).
"""

from flask import Flask, request, jsonify
import random
import re
import logging
from datetime import datetime

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ─── Sentiment keyword dictionaries ──────────────────────────────────────────
POSITIVE_WORDS = [
    'good', 'great', 'excellent', 'amazing', 'awesome', 'love', 'like',
    'best', 'wonderful', 'fantastic', 'beautiful', 'nice', 'perfect', 'happy',
    'enjoy', 'glad', 'pleased', 'impressive', 'outstanding', 'brilliant',
    'superb', 'terrific', 'marvelous', 'delightful', 'remarkable', 'stellar'
]

NEGATIVE_WORDS = [
    'bad', 'terrible', 'awful', 'horrible', 'hate', 'dislike', 'worst',
    'poor', 'disappointing', 'disgusting', 'ugly', 'boring', 'broken',
    'failed', 'wrong', 'sad', 'angry', 'upset', 'annoyed', 'frustrated',
    'useless', 'garbage', 'trash', 'disaster', 'pathetic', 'dreadful'
]


def analyze_sentiment(text: str, target: str = None) -> dict:
    """
    Mock sentiment analysis using keyword matching + randomness.
    Returns sentiment probabilities.
    """
    text_lower = text.lower() if text else ''
    target_lower = target.lower() if target else ''

    # Count keyword occurrences
    pos_count = sum(1 for w in POSITIVE_WORDS if w in text_lower)
    neg_count = sum(1 for w in NEGATIVE_WORDS if w in text_lower)

    # Check if target appears near sentiment words (simplified)
    if target_lower and target_lower in text_lower:
        idx = text_lower.find(target_lower)
        context = text_lower[max(0, idx-50):idx+50]
        pos_count += sum(1 for w in POSITIVE_WORDS if w in context)
        neg_count += sum(1 for w in NEGATIVE_WORDS if w in context)

    # Determine base sentiment
    if pos_count > neg_count:
        base = 'positive'
        base_prob = random.uniform(0.55, 0.90)
    elif neg_count > pos_count:
        base = 'negative'
        base_prob = random.uniform(0.55, 0.88)
    else:
        base = 'neutral'
        base_prob = random.uniform(0.45, 0.75)

    # Generate probability distribution
    remaining = 1.0 - base_prob
    other1 = random.uniform(0.02, remaining * 0.7)
    other2 = remaining - other1

    if base == 'positive':
        pos = base_prob
        neu = other1 if other1 > other2 else other2
        neg = other2 if other1 > other2 else other1
    elif base == 'negative':
        neg = base_prob
        neu = other1 if other1 > other2 else other2
        pos = other2 if other1 > other2 else other1
    else:
        neu = base_prob
        pos = other1 if other1 > other2 else other2
        neg = other2 if other1 > other2 else other1

    # Normalize
    total = pos + neu + neg
    pos, neu, neg = pos/total, neu/total, neg/total

    confidence = max(pos, neu, neg)

    return {
        'sentiment': base,
        'confidence': round(confidence, 4),
        'probabilities': {
            'positive': round(pos, 4),
            'neutral': round(neu, 4),
            'negative': round(neg, 4)
        }
    }


def extract_targets_from_text(content: str) -> list:
    """
    Rule-based target extraction:
    - @mentions
    - #hashtags (first letter capitalized)
    - Capitalized noun phrases (2+ words)
    """
    targets = set()

    # Extract @mentions (remove @ prefix)
    mentions = re.findall(r'@(\w+)', content)
    targets.update(m for m in mentions if len(m) > 1)

    # Extract capitalized phrases (e.g., "iPhone 15", "Tim Cook")
    cap_phrases = re.findall(r'\b[A-Z][a-z]+(?:\s+[A-Z][a-z]+)+\b', content)
    targets.update(cap_phrases)

    # Extract single capitalized words (not at sentence start)
    words = content.split()
    for i, word in enumerate(words):
        clean_word = re.sub(r'[^a-zA-Z]', '', word)
        if (i > 0 and clean_word and clean_word[0].isupper()
                and len(clean_word) > 3
                and clean_word.lower() not in {'this', 'that', 'with', 'from',
                                               'have', 'been', 'they', 'their',
                                               'what', 'when', 'where', 'which'}):
            targets.add(clean_word)

    return list(targets)[:5]  # Return at most 5 targets


# ─── API Endpoints ────────────────────────────────────────────────────────────

@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({
        'code': 200,
        'status': 'UP',
        'service': 'ABMSA Model Service',
        'timestamp': datetime.now().isoformat(),
        'model': 'Mock-ABSA-v1.0'
    })


@app.route('/api/predict', methods=['POST'])
def predict():
    """Single sentiment prediction."""
    try:
        data = request.get_json(force=True)
        text = data.get('text', '') or data.get('content', '')
        target = data.get('target', '')
        image_url = data.get('imageUrl', '') or data.get('image_url', '')

        if not text:
            return jsonify({'code': 400, 'message': 'text is required'}), 400

        result = analyze_sentiment(text, target)
        logger.info(f"Predict: target={target!r}, sentiment={result['sentiment']}")

        return jsonify({
            'code': 200,
            'data': {
                'target': target,
                'text': text,
                'imageUrl': image_url,
                **result
            }
        })
    except Exception as e:
        logger.error(f"Predict error: {e}")
        return jsonify({'code': 500, 'message': str(e)}), 500


@app.route('/api/predict/batch', methods=['POST'])
def predict_batch():
    """Batch sentiment prediction."""
    try:
        data = request.get_json(force=True)
        items = data.get('items', [])

        if not items:
            return jsonify({'code': 400, 'message': 'items list is required'}), 400

        results = []
        for item in items:
            text = item.get('text', '') or item.get('content', '')
            target = item.get('target', '')
            image_url = item.get('imageUrl', '') or item.get('image_url', '')
            result = analyze_sentiment(text, target)
            results.append({
                'target': target,
                'text': text,
                'imageUrl': image_url,
                **result
            })

        logger.info(f"Batch predict: {len(results)} items processed")
        return jsonify({'code': 200, 'data': {'results': results, 'count': len(results)}})
    except Exception as e:
        logger.error(f"Batch predict error: {e}")
        return jsonify({'code': 500, 'message': str(e)}), 500


@app.route('/api/extract-targets', methods=['POST'])
def extract_targets():
    """Extract aspect targets from tweet content."""
    try:
        data = request.get_json(force=True)
        content = data.get('content', '')

        if not content:
            return jsonify({'code': 400, 'message': 'content is required'}), 400

        targets = extract_targets_from_text(content)
        logger.info(f"Extract targets: found {len(targets)} targets")

        return jsonify({
            'code': 200,
            'data': {'targets': targets}
        })
    except Exception as e:
        logger.error(f"Extract targets error: {e}")
        return jsonify({'code': 500, 'message': str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
