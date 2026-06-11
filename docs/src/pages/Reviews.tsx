import { Star, MessageSquare } from 'lucide-react';
import { motion } from 'framer-motion';

const reviews = [
  { id: 1, user: "@cypher_punk", role: "DevOps Engineer", text: "Finally an Android launcher that speaks my language. The CLI engine saves me dozens of taps a day, and the DDD architecture is a joy to read." },
  { id: 2, user: "@android_dev23", role: "Android Developer", text: "The way Tensor handles immersive mode and dynamic status bars without polling is genius. Peak Jetpack Compose performance." },
  { id: 3, user: "@netsec_pro", role: "Security Auditor", text: "The built-in /sec tools and the fact that it doesn't request unnecessary permissions by default makes this my daily driver. Highly recommended." },
  { id: 4, user: "@minimalist", role: "Power User", text: "Zero clutter. Pure functionality. The smart ranking algorithm works flawlessly for my muscle memory." },
];

const Reviews = () => {
  return (
    <div className="py-12">
      <div className="text-center max-w-2xl mx-auto mb-16">
        <h1 className="text-4xl font-bold mb-4 flex items-center justify-center gap-3">
          <Star className="text-primary" size={32} /> User Reviews
        </h1>
        <p className="text-on-surface text-lg">What developers and power users are saying about Tensor Launcher.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {reviews.map((review, i) => (
          <motion.div 
            key={review.id}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.1 }}
            className="bg-surface border border-border p-6 rounded-lg relative"
          >
            <MessageSquare className="absolute top-6 right-6 text-border opacity-50" size={40} />
            <div className="flex items-center gap-1 text-primary mb-4">
              <Star size={16} fill="currentColor" />
              <Star size={16} fill="currentColor" />
              <Star size={16} fill="currentColor" />
              <Star size={16} fill="currentColor" />
              <Star size={16} fill="currentColor" />
            </div>
            <p className="text-on-background mb-6 text-lg italic">"{review.text}"</p>
            <div className="font-mono text-sm">
              <span className="text-primary font-bold">{review.user}</span>
              <span className="text-on-surface ml-2">// {review.role}</span>
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  );
};

export default Reviews;
