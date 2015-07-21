# fpMLN
Grounding Network Sampling for Inference on triplets (or knowledge bases).

It is a collection of experiments codes, including mainly two published papaers. (more in future)

[1] Zhengya Sun, Zhuoyu Wei, Jue Wang and Hongwei Hao. Scalable Learning for Structure in Markov Logic Networks[C]. Workshops at the Twenty-Eighth AAAI Conference on Artificial Intelligence. 2014.

[2] Zhuoyu Wei, Jun Zhao, KangLiu, Zhenyu Qi, Zhengya Sun and Guanhua Tian. Large-scale Knowledge Base Completion: Inferring via Grounding Network Sampling over Selected Instances. CIKM, 2015.

If you use this code, you should cite above papers based on your purpose. (one of them or both)


Two main classes in our code folder you should pay attention: wzy.main.SemanticLinkPrediction and wzy.main.KnowledgeBaseCompletion. 

If you want to predict one type link in some networks, such as citation network, social network, or user&item network, you should use the SemanticLinkPrediction, and cite the paper [1]. If you want to process knowledge base completion for some knowledge base, which have lots of relations, you should use the KnowledgeBaseCompletion, and cite both [1] and [2]. Remember that both of them are discriminative methods, which means you only predict one type of relation (or link) when running them once. If you want to run it on multi-relation knowledge bases, i.e. Freebase, you need write a simple script to run the code many times with different parameters which are specific for relations.

We use two examples to explain how we run these two processes.

1. 
2.
