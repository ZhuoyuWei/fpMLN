# fpMLN
Grounding Network Sampling for Inference on triplets (or knowledge bases).

It is a collection of experiments codes, including mainly two published papaers. (more in future)

[1] Zhengya Sun, Zhuoyu Wei, Jue Wang and Hongwei Hao. Scalable Learning for Structure in Markov Logic Networks[C]. Workshops at the Twenty-Eighth AAAI Conference on Artificial Intelligence. 2014.

[2] Zhuoyu Wei, Jun Zhao, KangLiu, Zhenyu Qi, Zhengya Sun and Guanhua Tian. Large-scale Knowledge Base Completion: Inferring via Grounding Network Sampling over Selected Instances. CIKM, 2015.

If you use this code, you should cite above papers based on your purpose. (one of them or both)


Two main classes in our code folder you should pay attention: wzy.main.SemanticLinkPrediction and wzy.main.KnowledgeBaseCompletion. 

If you want to predict one type link in some networks, such as citation network, social network, or user&item network, you should use the SemanticLinkPrediction, and cite the paper [1]. If you want to process knowledge base completion for some knowledge base, which have lots of relations, you should use the KnowledgeBaseCompletion, and cite both [1] and [2]. Remember that both of them are discriminative methods, which means you only predict one type of relation (or link) when running them once. If you want to run it on multi-relation knowledge bases, i.e. Freebase, you need write a simple script to run the code many times with different parameters which are specific for relations.

We use two examples to explain how we run these two processes.

1.Link prediction

We use the dataset UW-CSE Dataset in Alchemy (http://alchemy.cs.washington.edu/data/uw-cse/). This data set consists of information about the University of Washington Department of Computer Science and Engineering.

If we want to predict missing links in UW-CSE, we need represent data as tuples, and our implement is able to handle not only triplets but also N-tuples. However, we cannot handle unary tuple, you need transform n(a) to r(a,n) first. In this way, you can keep all your lines in dataset as the form, r(e1,e2,...)

Then you choose wzy.main.SemanticLinkPrediction as the main function entrance. We need a parameter file to set all values in our experiment. The example parameter file is included in examples folder.

The meanings of all parameters are described as follows:

inputInfoFile: a file describe all relation types and entity types.
trainDbFile: tripelts file to be used to train formulas structures and weights.
testDbFile: In test process, all triplets in this file are all treated as evidence.
queryDbFile: In test process, we need inference for all triplets in this file.
MaxRound: In learning process, random walk MaxRound times for one seed.
iMaxRound: similar as MaxRound, in inference process, random walk MaxRound times for one seed.
MinLength and MaxLength: they represents minimum and maximum length of formulas learned in structure learning process.
ThreadNum: our implement can run as multithreaded program, you can use this parameter to control the size of the thread pool.
RandomWalkNum: the times of randomly choosing starting seed for random walk.
Query: the relation type you want to predict. Keep maid that we remove all query type triplets in evidences by default.
RestartProbability: the probability of restart from the seed state during random walk process.
Lamda: step size when update weights of formulas.
Upsilon:  error threshold, less then it and the learning stop. 
C: regular coefficient
K: the size of minibanch.
MaxIterator: the maximum iterator times during learning weights.
falseRate: produce falseRate false head for grounding formulas, when random walk for grounding formulas.
BaseDir: the result directory.
islorr: the way of select random walk seed.


2.
